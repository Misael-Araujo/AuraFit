package com.aurafit.identity.application;

import com.aurafit.identity.domain.EmailVerificationToken;
import com.aurafit.identity.domain.RefreshToken;
import com.aurafit.identity.domain.User;
import com.aurafit.identity.domain.UserRole;
import com.aurafit.shared.config.AuraFitProperties;
import com.aurafit.shared.error.ApiException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final String INVALID_CREDENTIALS = "Invalid email or password.";

  private final UserRepositoryPort users;
  private final EmailVerificationTokenRepositoryPort verificationTokens;
  private final RefreshTokenRepositoryPort refreshTokens;
  private final EmailSenderPort emailSender;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final AuraFitProperties properties;
  private final TokenFactory tokenFactory;
  private final UserMapper userMapper;
  private final LoginAttemptProtector loginAttemptProtector;
  private final JdbcTemplate jdbcTemplate;

  public AuthService(
      UserRepositoryPort users,
      EmailVerificationTokenRepositoryPort verificationTokens,
      RefreshTokenRepositoryPort refreshTokens,
      EmailSenderPort emailSender,
      PasswordEncoder passwordEncoder,
      JwtEncoder jwtEncoder,
      AuraFitProperties properties,
      TokenFactory tokenFactory,
      UserMapper userMapper,
      LoginAttemptProtector loginAttemptProtector,
      JdbcTemplate jdbcTemplate) {
    this.users = users;
    this.verificationTokens = verificationTokens;
    this.refreshTokens = refreshTokens;
    this.emailSender = emailSender;
    this.passwordEncoder = passwordEncoder;
    this.jwtEncoder = jwtEncoder;
    this.properties = properties;
    this.tokenFactory = tokenFactory;
    this.userMapper = userMapper;
    this.loginAttemptProtector = loginAttemptProtector;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Transactional
  public UserView register(String email, String password, String displayName, LocalDate birthDate) {
    String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
    if (birthDate.isAfter(LocalDate.now().minusYears(18))) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "age_restriction",
          "AuraFit is available to adults aged 18 or older.");
    }
    if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "invalid_password", "Password must not exceed 72 UTF-8 bytes.");
    }
    if (users.findByEmail(normalizedEmail).isPresent()) {
      throw new ApiException(
          HttpStatus.CONFLICT, "email_already_registered", "An account already uses this email.");
    }

    Instant now = Instant.now();
    User user =
        users.save(
            new User(
                UUID.randomUUID(),
                normalizedEmail,
                passwordEncoder.encode(password),
                displayName.trim(),
                birthDate,
                false,
                UserRole.USER,
                now));
    String rawToken = tokenFactory.newToken();
    verificationTokens.save(
        new EmailVerificationToken(
            UUID.randomUUID(),
            user.id(),
            TokenHasher.hash(rawToken),
            now.plus(properties.identity().verification().tokenTtl()),
            null));
    emailSender.sendVerificationToken(user.email(), rawToken);
    return userMapper.toView(user);
  }

  @Transactional
  public void verifyEmail(String rawToken) {
    Instant now = Instant.now();
    EmailVerificationToken token =
        verificationTokens
            .findByTokenHash(TokenHasher.hash(rawToken))
            .filter(value -> value.isUsableAt(now))
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.GONE,
                        "verification_token_invalid",
                        "The verification token is invalid or expired."));
    User user =
        users
            .findById(token.userId())
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND,
                        "account_not_found",
                        "The account no longer exists."));
    verificationTokens.save(token.consumeAt(now));
    users.save(user.verified());
  }

  @Transactional
  public AuthTokens login(String email, String password, String clientAddress) {
    String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
    loginAttemptProtector.checkAllowed(normalizedEmail, clientAddress);
    User user =
        users
            .findByEmail(normalizedEmail)
            .orElseThrow(
                () -> {
                  loginAttemptProtector.recordFailure(normalizedEmail, clientAddress);
                  return new ApiException(
                      HttpStatus.UNAUTHORIZED, "invalid_credentials", INVALID_CREDENTIALS);
                });
    if (!passwordEncoder.matches(password, user.passwordHash())) {
      loginAttemptProtector.recordFailure(normalizedEmail, clientAddress);
      throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials", INVALID_CREDENTIALS);
    }
    if (!user.emailVerified()) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "email_not_verified", "Verify your email before signing in.");
    }

    loginAttemptProtector.recordSuccess(normalizedEmail, clientAddress);
    UUID familyId = UUID.randomUUID();
    return issueTokens(user, familyId, Instant.now());
  }

  @Transactional(noRollbackFor = ApiException.class)
  public AuthTokens refresh(String rawRefreshToken) {
    Instant now = Instant.now();
    RefreshToken current =
        refreshTokens
            .findForUpdateByTokenHash(TokenHasher.hash(rawRefreshToken))
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "refresh_token_invalid",
                        "The refresh token is invalid or expired."));
    if (current.revokedAt() != null) {
      refreshTokens.revokeFamily(current.familyId(), now);
      throw new ApiException(
          HttpStatus.UNAUTHORIZED,
          "refresh_token_reused",
          "The refresh token has already been used. Sign in again.");
    }
    if (!current.expiresAt().isAfter(now)) {
      throw new ApiException(
          HttpStatus.UNAUTHORIZED,
          "refresh_token_invalid",
          "The refresh token is invalid or expired.");
    }
    User user =
        users
            .findById(current.userId())
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "refresh_token_invalid",
                        "The refresh token is invalid or expired."));

    String nextRawToken = tokenFactory.newToken();
    String nextHash = TokenHasher.hash(nextRawToken);
    refreshTokens.save(current.rotate(nextHash, now));
    refreshTokens.save(newRefreshToken(user.id(), current.familyId(), nextHash, now));
    return new AuthTokens(
        createAccessToken(user, now),
        nextRawToken,
        "Bearer",
        now.plus(properties.security().jwt().accessTokenTtl()));
  }

  @Transactional
  public void logout(UUID userId, String rawRefreshToken) {
    refreshTokens
        .findForUpdateByTokenHash(TokenHasher.hash(rawRefreshToken))
        .filter(token -> token.userId().equals(userId) && token.revokedAt() == null)
        .ifPresent(token -> refreshTokens.revokeFamily(token.familyId(), Instant.now()));
  }

  @Transactional
  public void deleteAccount(UUID userId, String currentPassword, String confirmationEmail) {
    User user =
        users
            .findById(userId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND,
                        "account_not_found",
                        "The account no longer exists."));

    if (!user.email().equalsIgnoreCase(confirmationEmail.trim())) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "confirmation_email_mismatch",
          "The confirmation email does not match the account email.");
    }
    if (!passwordEncoder.matches(currentPassword, user.passwordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials", INVALID_CREDENTIALS);
    }

    users.deleteById(userId);
    jdbcTemplate.update(
        "insert into audit_event (id, user_id, event_type, event_details, created_at) values (?, ?, ?, ?, ?)",
        UUID.randomUUID(),
        userId,
        "account_deleted",
        "{\"confirmedEmail\":\"" + user.email() + "\",\"deletedAt\":\"" + Instant.now() + "\"}",
        Instant.now());
  }

  @Transactional(readOnly = true)
  public UserView getCurrentUser(UUID userId) {
    return users
        .findById(userId)
        .map(userMapper::toView)
        .orElseThrow(
            () ->
                new ApiException(
                    HttpStatus.NOT_FOUND, "account_not_found", "The account no longer exists."));
  }

  private AuthTokens issueTokens(User user, UUID familyId, Instant now) {
    String rawRefreshToken = tokenFactory.newToken();
    String hash = TokenHasher.hash(rawRefreshToken);
    refreshTokens.save(newRefreshToken(user.id(), familyId, hash, now));
    return new AuthTokens(
        createAccessToken(user, now),
        rawRefreshToken,
        "Bearer",
        now.plus(properties.security().jwt().accessTokenTtl()));
  }

  private RefreshToken newRefreshToken(UUID userId, UUID familyId, String hash, Instant now) {
    return new RefreshToken(
        UUID.randomUUID(),
        userId,
        familyId,
        hash,
        now.plus(java.time.Duration.ofDays(30)),
        null,
        null,
        now);
  }

  private String createAccessToken(User user, Instant now) {
    Instant expiresAt = now.plus(properties.security().jwt().accessTokenTtl());
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(properties.security().jwt().issuer())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(user.id().toString())
            .claim("email", user.email())
            .claim("roles", List.of(user.role().name()))
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
