package com.aurafit.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record EmailVerificationToken(
    UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant consumedAt) {

  public boolean isUsableAt(Instant now) {
    return consumedAt == null && expiresAt.isAfter(now);
  }

  public EmailVerificationToken consumeAt(Instant now) {
    return new EmailVerificationToken(id, userId, tokenHash, expiresAt, now);
  }
}
