package com.aurafit.consent.application;

import com.aurafit.consent.domain.ConsentPurpose;
import com.aurafit.consent.domain.ConsentRecord;
import com.aurafit.shared.error.ApiException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentService {

  public static final String CURRENT_POLICY_VERSION = "2026-01";

  private final ConsentRepositoryPort repository;

  public ConsentService(ConsentRepositoryPort repository) {
    this.repository = repository;
  }

  @Transactional
  public ConsentRecord record(
      UUID userId, ConsentPurpose purpose, boolean granted, String policyVersion) {
    if (policyVersion == null || policyVersion.isBlank() || policyVersion.length() > 40) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "invalid_policy_version", "A valid policy version is required.");
    }
    return repository.save(
        new ConsentRecord(
            UUID.randomUUID(), userId, purpose, granted, policyVersion.trim(), Instant.now()));
  }

  @Transactional(readOnly = true)
  public List<ConsentRecord> current(UUID userId) {
    return repository.findByUserId(userId).stream()
        .collect(
            java.util.stream.Collectors.toMap(
                ConsentRecord::purpose,
                record -> record,
                (left, right) -> left.recordedAt().isAfter(right.recordedAt()) ? left : right))
        .values()
        .stream()
        .sorted(Comparator.comparing(ConsentRecord::purpose))
        .toList();
  }

  @Transactional(readOnly = true)
  public boolean hasCurrentGrantedConsent(UUID userId, ConsentPurpose purpose) {
    return repository
        .findLatestByUserIdAndPurpose(userId, purpose)
        .map(record -> record.granted() && CURRENT_POLICY_VERSION.equals(record.policyVersion()))
        .orElse(false);
  }

  @Transactional(readOnly = true)
  public void requireCurrentGrantedConsent(UUID userId, ConsentPurpose purpose, String message) {
    if (!hasCurrentGrantedConsent(userId, purpose)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN,
          "consent_required",
          message != null ? message : "Consent is required before this operation.");
    }
  }
}
