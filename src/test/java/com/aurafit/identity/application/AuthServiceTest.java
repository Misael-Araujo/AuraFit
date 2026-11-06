package com.aurafit.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aurafit.identity.domain.EmailVerificationToken;
import com.aurafit.identity.domain.RefreshToken;
import com.aurafit.identity.domain.User;
import com.aurafit.identity.domain.UserRole;
import com.aurafit.shared.config.AuraFitProperties;
import com.aurafit.shared.error.ApiException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepositoryPort users;
  @Mock private EmailVerificationTokenRepositoryPort verificationTokens;
  @Mock private RefreshTokenRepositoryPort refreshTokens;
  @Mock private EmailSenderPort emailSender;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtEncoder jwtEncoder;
  @Mock private UserMapper userMapper;
  @Mock private JdbcTemplate jdbcTemplate;

  private AuthService service;
  private final LoginAttemptProtector loginAttemptProtector = new LoginAttemptProtector();
  private final TokenFactory tokenFactory = new TokenFactory();
  private final AuraFitProperties properties =
      new AuraFitProperties(
          "http://localhost:3000",
          new AuraFitProperties.Cors(List.of("http://localhost:3000")),
          new AuraFitProperties.Security(
              new AuraFitProperties.Jwt(
                  "aurafit",
                  "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                  Duration.ofMinutes(15))),
          new AuraFitProperties.Identity(new AuraFitProperties.Verification(Duration.ofHours(24))),
          new AuraFitProperties.Media(
              new AuraFitProperties.Storage(
                  "http://localhost:9000",
                  "aurafit-private",
                  "us-east-1",
                  "aurafit-local",
                  "aurafit-minio-local-only",
                  true),
              new AuraFitProperties.Upload(10 * 1024 * 1024, 25_000_000L, 32, 32, 12_000, 12_000)),
          new AuraFitProperties.RateLimit(20));
  private final Jwt jwt =
      Jwt.withTokenValue("access-token")
          .header("alg", "HS256")
          .claim("sub", UUID.randomUUID().toString())
          .build();

  @BeforeEach
  void setUp() {
    service =
        new AuthService(
            users,
            verificationTokens,
            refreshTokens,
            emailSender,
            passwordEncoder,
            jwtEncoder,
            properties,
            tokenFactory,
            userMapper,
            loginAttemptProtector,
            jdbcTemplate);
  }

  @Test
  void registersAdultAndStoresOnlyVerificationTokenHash() {
    User user =
        new User(
            UUID.randomUUID(),
            "user@example.com",
            "encoded-password",
            "Example",
            LocalDate.of(1990, 1, 1),
            false,
            UserRole.USER,
            Instant.now());
    when(users.findByEmail("user@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("long-password-123")).thenReturn("encoded-password");
    when(users.save(any())).thenReturn(user);
    when(verificationTokens.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(userMapper.toView(user))
        .thenReturn(
            new UserView(
                user.id(),
                user.email(),
                user.displayName(),
                user.birthDate(),
                user.emailVerified(),
                user.createdAt()));

    UserView result =
        service.register(
            " USER@example.com ", "long-password-123", " Example ", LocalDate.of(1990, 1, 1));

    assertThat(result.email()).isEqualTo("user@example.com");
    ArgumentCaptor<EmailVerificationToken> token =
        ArgumentCaptor.forClass(EmailVerificationToken.class);
    verify(verificationTokens).save(token.capture());
    assertThat(token.getValue().tokenHash()).hasSize(64);
    verify(emailSender).sendVerificationToken(eq("user@example.com"), any());
  }

  @Test
  void rejectsMinorAndPasswordBeyondBcryptByteLimit() {
    assertThatThrownBy(
            () ->
                service.register(
                    "user@example.com",
                    "long-password-123",
                    "Example",
                    LocalDate.now().minusYears(17)))
        .isInstanceOf(ApiException.class);
    assertThatThrownBy(
            () ->
                service.register(
                    "user@example.com", "é".repeat(37), "Example", LocalDate.of(1990, 1, 1)))
        .isInstanceOf(ApiException.class);
    verify(users, never()).save(any());
  }

  @Test
  void rejectsDuplicateRegistration() {
    when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user(true)));

    assertThatThrownBy(
            () ->
                service.register(
                    "user@example.com", "long-password-123", "Example", LocalDate.of(1990, 1, 1)))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("already uses this email");
    verify(emailSender, never()).sendVerificationToken(any(), any());
  }

  @Test
  void verifiesAnUnusedEmailTokenAndConsumesIt() {
    Instant now = Instant.now();
    User user = user(false);
    EmailVerificationToken token =
        new EmailVerificationToken(
            UUID.randomUUID(),
            user.id(),
            TokenHasher.hash("verification-token"),
            now.plusSeconds(60),
            null);
    when(verificationTokens.findByTokenHash(token.tokenHash())).thenReturn(Optional.of(token));
    when(users.findById(user.id())).thenReturn(Optional.of(user));
    when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(verificationTokens.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.verifyEmail("verification-token");

    ArgumentCaptor<EmailVerificationToken> consumed =
        ArgumentCaptor.forClass(EmailVerificationToken.class);
    verify(verificationTokens).save(consumed.capture());
    assertThat(consumed.getValue().consumedAt()).isNotNull();
    verify(users).save(user.verified());
  }

  @Test
  void rejectsInvalidVerificationToken() {
    when(verificationTokens.findByTokenHash(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.verifyEmail("invalid"))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("invalid or expired");
  }

  @Test
  void issuesTokensForVerifiedUserAndRejectsBadOrUnverifiedCredentials() {
    User verifiedUser = user(true);
    when(users.findByEmail("user@example.com")).thenReturn(Optional.of(verifiedUser));
    when(passwordEncoder.matches("correct-password", verifiedUser.passwordHash())).thenReturn(true);
    when(passwordEncoder.matches("bad-password", verifiedUser.passwordHash())).thenReturn(false);
    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
    when(refreshTokens.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    AuthTokens tokens = service.login("USER@example.com", "correct-password", "127.0.0.1");

    assertThat(tokens.accessToken()).isEqualTo("access-token");
    assertThat(tokens.refreshToken()).isNotBlank();
    assertThat(tokens.tokenType()).isEqualTo("Bearer");
    assertThatThrownBy(() -> service.login("user@example.com", "bad-password", "127.0.0.1"))
        .isInstanceOf(ApiException.class);

    User unverifiedUser = user(false);
    when(users.findByEmail("unverified@example.com")).thenReturn(Optional.of(unverifiedUser));
    when(passwordEncoder.matches("correct-password", unverifiedUser.passwordHash()))
        .thenReturn(true);
    assertThatThrownBy(
            () -> service.login("unverified@example.com", "correct-password", "127.0.0.1"))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Verify your email");
  }

  @Test
  void rotatesRefreshTokenAndRevokesFamilyOnReuse() {
    User user = user(true);
    UUID familyId = UUID.randomUUID();
    RefreshToken current =
        new RefreshToken(
            UUID.randomUUID(),
            user.id(),
            familyId,
            TokenHasher.hash("current-refresh"),
            Instant.now().plusSeconds(60),
            null,
            null,
            Instant.now());
    when(refreshTokens.findForUpdateByTokenHash(current.tokenHash()))
        .thenReturn(Optional.of(current));
    when(users.findById(user.id())).thenReturn(Optional.of(user));
    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
    when(refreshTokens.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    AuthTokens next = service.refresh("current-refresh");

    assertThat(next.refreshToken()).isNotEqualTo("current-refresh");
    ArgumentCaptor<RefreshToken> stored = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokens, org.mockito.Mockito.times(2)).save(stored.capture());
    assertThat(stored.getAllValues().get(0).revokedAt()).isNotNull();
    assertThat(stored.getAllValues().get(0).replacedByHash())
        .isEqualTo(stored.getAllValues().get(1).tokenHash());

    RefreshToken replayed = current.revokeAt(Instant.now());
    when(refreshTokens.findForUpdateByTokenHash(TokenHasher.hash("replayed")))
        .thenReturn(Optional.of(replayed));
    assertThatThrownBy(() -> service.refresh("replayed"))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("already been used");
    verify(refreshTokens).revokeFamily(eq(familyId), any());
  }

  @Test
  void rejectsExpiredRefreshTokensAndLogsOutOnlyTheOwnersFamily() {
    UUID familyId = UUID.randomUUID();
    RefreshToken expired =
        new RefreshToken(
            UUID.randomUUID(),
            UUID.randomUUID(),
            familyId,
            TokenHasher.hash("expired"),
            Instant.now().minusSeconds(1),
            null,
            null,
            Instant.now().minusSeconds(60));
    when(refreshTokens.findForUpdateByTokenHash(expired.tokenHash()))
        .thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> service.refresh("expired")).isInstanceOf(ApiException.class);
    service.logout(UUID.randomUUID(), "expired");
    verify(refreshTokens, never()).revokeFamily(eq(familyId), any());
  }

  @Test
  void returnsTheCurrentUserOrNotFound() {
    User user = user(true);
    when(users.findById(user.id())).thenReturn(Optional.of(user));
    when(userMapper.toView(user))
        .thenReturn(
            new UserView(
                user.id(),
                user.email(),
                user.displayName(),
                user.birthDate(),
                user.emailVerified(),
                user.createdAt()));

    assertThat(service.getCurrentUser(user.id()).id()).isEqualTo(user.id());
    assertThatThrownBy(() -> service.getCurrentUser(UUID.randomUUID()))
        .isInstanceOf(ApiException.class);
  }

  private User user(boolean emailVerified) {
    return new User(
        UUID.randomUUID(),
        "user@example.com",
        "encoded-password",
        "Example",
        LocalDate.of(1990, 1, 1),
        emailVerified,
        UserRole.USER,
        Instant.now());
  }
}
