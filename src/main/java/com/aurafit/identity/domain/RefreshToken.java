package com.aurafit.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record RefreshToken(
    UUID id,
    UUID userId,
    UUID familyId,
    String tokenHash,
    Instant expiresAt,
    Instant revokedAt,
    String replacedByHash,
    Instant createdAt) {

  public boolean isUsableAt(Instant now) {
    return revokedAt == null && expiresAt.isAfter(now);
  }

  public RefreshToken rotate(String nextHash, Instant now) {
    return new RefreshToken(id, userId, familyId, tokenHash, expiresAt, now, nextHash, createdAt);
  }

  public RefreshToken revokeAt(Instant now) {
    return new RefreshToken(
        id, userId, familyId, tokenHash, expiresAt, now, replacedByHash, createdAt);
  }
}
