package com.aurafit.identity.infrastructure.persistence;

import com.aurafit.identity.domain.EmailVerificationToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_token")
public class EmailVerificationTokenEntity {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "consumed_at")
  private Instant consumedAt;

  protected EmailVerificationTokenEntity() {}

  private EmailVerificationTokenEntity(EmailVerificationToken token) {
    id = token.id();
    userId = token.userId();
    tokenHash = token.tokenHash();
    expiresAt = token.expiresAt();
    consumedAt = token.consumedAt();
  }

  static EmailVerificationTokenEntity from(EmailVerificationToken token) {
    return new EmailVerificationTokenEntity(token);
  }

  EmailVerificationToken toDomain() {
    return new EmailVerificationToken(id, userId, tokenHash, expiresAt, consumedAt);
  }
}
