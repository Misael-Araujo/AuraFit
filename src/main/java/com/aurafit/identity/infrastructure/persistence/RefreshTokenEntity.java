package com.aurafit.identity.infrastructure.persistence;

import com.aurafit.identity.domain.RefreshToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenEntity {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "family_id", nullable = false)
  private UUID familyId;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "replaced_by_hash", length = 64)
  private String replacedByHash;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected RefreshTokenEntity() {}

  private RefreshTokenEntity(RefreshToken token) {
    id = token.id();
    userId = token.userId();
    familyId = token.familyId();
    tokenHash = token.tokenHash();
    expiresAt = token.expiresAt();
    revokedAt = token.revokedAt();
    replacedByHash = token.replacedByHash();
    createdAt = token.createdAt();
  }

  static RefreshTokenEntity from(RefreshToken token) {
    return new RefreshTokenEntity(token);
  }

  RefreshToken toDomain() {
    return new RefreshToken(
        id, userId, familyId, tokenHash, expiresAt, revokedAt, replacedByHash, createdAt);
  }
}
