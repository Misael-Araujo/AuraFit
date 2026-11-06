package com.aurafit.consent.infrastructure.persistence;

import com.aurafit.consent.domain.ConsentPurpose;
import com.aurafit.consent.domain.ConsentRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consent_record")
public class ConsentRecordEntity {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private ConsentPurpose purpose;

  @Column(nullable = false)
  private boolean granted;

  @Column(name = "policy_version", nullable = false, length = 40)
  private String policyVersion;

  @Column(name = "recorded_at", nullable = false)
  private Instant recordedAt;

  protected ConsentRecordEntity() {}

  private ConsentRecordEntity(ConsentRecord record) {
    id = record.id();
    userId = record.userId();
    purpose = record.purpose();
    granted = record.granted();
    policyVersion = record.policyVersion();
    recordedAt = record.recordedAt();
  }

  static ConsentRecordEntity from(ConsentRecord record) {
    return new ConsentRecordEntity(record);
  }

  ConsentRecord toDomain() {
    return new ConsentRecord(id, userId, purpose, granted, policyVersion, recordedAt);
  }
}
