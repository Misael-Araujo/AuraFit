package com.aurafit.consent.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConsentRecordTest {

  @Test
  void retainsPurposeGrantPolicyAndTimestamp() {
    Instant recordedAt = Instant.parse("2026-01-01T00:00:00Z");

    ConsentRecord record =
        new ConsentRecord(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ConsentPurpose.AVATAR_CREATION,
            false,
            "2026-01",
            recordedAt);

    assertThat(record.purpose()).isEqualTo(ConsentPurpose.AVATAR_CREATION);
    assertThat(record.granted()).isFalse();
    assertThat(record.policyVersion()).isEqualTo("2026-01");
    assertThat(record.recordedAt()).isEqualTo(recordedAt);
  }
}
