package com.aurafit.consent.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aurafit.consent.domain.ConsentPurpose;
import com.aurafit.consent.domain.ConsentRecord;
import com.aurafit.shared.error.ApiException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

  @Mock private ConsentRepositoryPort repository;

  @Test
  void recordsAValidatedConsentDecision() {
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    ConsentService service = new ConsentService(repository);

    ConsentRecord result =
        service.record(UUID.randomUUID(), ConsentPurpose.AVATAR_CREATION, true, " 2026-01 ");

    assertThat(result.granted()).isTrue();
    assertThat(result.policyVersion()).isEqualTo("2026-01");
    assertThat(result.recordedAt()).isNotNull();
  }

  @Test
  void rejectsMissingOrTooLongPolicyVersion() {
    ConsentService service = new ConsentService(repository);

    assertThatThrownBy(
            () -> service.record(UUID.randomUUID(), ConsentPurpose.AVATAR_CREATION, true, " "))
        .isInstanceOf(ApiException.class);
    assertThatThrownBy(
            () ->
                service.record(
                    UUID.randomUUID(), ConsentPurpose.AVATAR_CREATION, true, "v".repeat(41)))
        .isInstanceOf(ApiException.class);
  }

  @Test
  void returnsOnlyLatestDecisionPerPurposeInStableOrder() {
    UUID userId = UUID.randomUUID();
    ConsentRecord older =
        new ConsentRecord(
            UUID.randomUUID(),
            userId,
            ConsentPurpose.AVATAR_CREATION,
            true,
            "v1",
            java.time.Instant.parse("2026-01-01T00:00:00Z"));
    ConsentRecord newer =
        new ConsentRecord(
            UUID.randomUUID(),
            userId,
            ConsentPurpose.AVATAR_CREATION,
            false,
            "v2",
            java.time.Instant.parse("2026-01-02T00:00:00Z"));
    ConsentRecord marketing =
        new ConsentRecord(
            UUID.randomUUID(),
            userId,
            ConsentPurpose.MARKETING,
            true,
            "v1",
            java.time.Instant.parse("2026-01-01T00:00:00Z"));
    when(repository.findByUserId(userId)).thenReturn(List.of(older, newer, marketing));

    List<ConsentRecord> result = new ConsentService(repository).current(userId);

    assertThat(result)
        .extracting(ConsentRecord::purpose)
        .containsExactly(ConsentPurpose.AVATAR_CREATION, ConsentPurpose.MARKETING);
    assertThat(result.getFirst().granted()).isFalse();
  }
}
