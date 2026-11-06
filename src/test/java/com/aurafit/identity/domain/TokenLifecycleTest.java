package com.aurafit.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TokenLifecycleTest {

  @Test
  void verificationTokenCanBeConsumedOnlyBeforeExpiry() {
    Instant now = Instant.parse("2026-01-01T00:00:00Z");
    EmailVerificationToken token =
        new EmailVerificationToken(
            UUID.randomUUID(), UUID.randomUUID(), "hash", now.plusSeconds(5), null);

    assertThat(token.isUsableAt(now)).isTrue();
    assertThat(token.isUsableAt(now.plusSeconds(5))).isFalse();
    assertThat(token.consumeAt(now).isUsableAt(now)).isFalse();
  }

  @Test
  void refreshTokenRotationAndRevocationAreImmutable() {
    Instant now = Instant.parse("2026-01-01T00:00:00Z");
    RefreshToken token =
        new RefreshToken(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "hash",
            now.plusSeconds(60),
            null,
            null,
            now);

    RefreshToken rotated = token.rotate("next-hash", now.plusSeconds(1));
    RefreshToken revoked = token.revokeAt(now.plusSeconds(2));

    assertThat(token.isUsableAt(now)).isTrue();
    assertThat(rotated.revokedAt()).isEqualTo(now.plusSeconds(1));
    assertThat(rotated.replacedByHash()).isEqualTo("next-hash");
    assertThat(revoked.isUsableAt(now.plusSeconds(2))).isFalse();
  }
}
