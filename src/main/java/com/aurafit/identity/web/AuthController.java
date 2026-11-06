package com.aurafit.identity.web;

import com.aurafit.identity.application.AuthService;
import com.aurafit.identity.application.AuthTokens;
import com.aurafit.identity.application.DataExportService;
import com.aurafit.identity.application.UserView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final DataExportService dataExportService;

  public AuthController(AuthService authService, DataExportService dataExportService) {
    this.authService = authService;
    this.dataExportService = dataExportService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public RegistrationResponse register(@Valid @RequestBody RegisterRequest request) {
    UserView user =
        authService.register(
            request.email(), request.password(), request.displayName(), request.birthDate());
    return new RegistrationResponse("Check your email for an account verification link.", user);
  }

  @PostMapping("/verify-email")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
    authService.verifyEmail(request.token());
  }

  @PostMapping("/login")
  public AuthTokens login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
    return authService.login(request.email(), request.password(), servletRequest.getRemoteAddr());
  }

  @PostMapping("/refresh")
  public AuthTokens refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return authService.refresh(request.refreshToken());
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @SecurityRequirement(name = "bearerAuth")
  public void logout(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RefreshTokenRequest request) {
    authService.logout(UUID.fromString(jwt.getSubject()), request.refreshToken());
  }

  @GetMapping("/me")
  @SecurityRequirement(name = "bearerAuth")
  public UserView me(@AuthenticationPrincipal Jwt jwt) {
    return authService.getCurrentUser(UUID.fromString(jwt.getSubject()));
  }

  @PostMapping("/me/data-export")
  @SecurityRequirement(name = "bearerAuth")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public DataExportResponse dataExport(@AuthenticationPrincipal Jwt jwt) {
    return dataExportService.export(UUID.fromString(jwt.getSubject()));
  }

  @DeleteMapping("/me")
  @SecurityRequirement(name = "bearerAuth")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMe(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DeleteAccountRequest request) {
    authService.deleteAccount(
        UUID.fromString(jwt.getSubject()), request.currentPassword(), request.confirmationEmail());
  }
}
