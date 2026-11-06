package com.aurafit.generation.infrastructure.persistence;

import com.aurafit.generation.domain.GenerationJob;
import com.aurafit.generation.domain.GenerationJobState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "generation_job")
public class GenerationJobEntity {
  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "job_type", nullable = false)
  private String jobType;

  @Column(name = "payload", length = 2048)
  private String payload;

  @Column(name = "idempotency_key", length = 128)
  private String idempotencyKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private GenerationJobState status;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "max_attempts", nullable = false)
  private int maxAttempts;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "scheduled_for", nullable = false)
  private Instant scheduledFor;

  @Column(name = "lease_until")
  private Instant leaseUntil;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "last_error", length = 1000)
  private String lastError;

  public GenerationJobEntity() {}

  public static GenerationJobEntity fromAggregate(GenerationJob job) {
    GenerationJobEntity entity = new GenerationJobEntity();
    entity.id = job.id();
    entity.userId = job.userId();
    entity.jobType = job.jobType();
    entity.payload = job.payload();
    entity.idempotencyKey = job.idempotencyKey();
    entity.status = job.status();
    entity.attemptCount = job.attemptCount();
    entity.maxAttempts = job.maxAttempts();
    entity.createdAt = job.createdAt();
    entity.updatedAt = job.updatedAt();
    entity.scheduledFor = job.scheduledFor();
    entity.leaseUntil = job.leaseUntil();
    entity.completedAt = job.completedAt();
    entity.lastError = job.lastError();
    return entity;
  }

  public GenerationJob toAggregate() {
    return new GenerationJob(
        id,
        userId,
        jobType,
        payload,
        idempotencyKey,
        status,
        attemptCount,
        maxAttempts,
        createdAt,
        updatedAt,
        scheduledFor,
        leaseUntil,
        completedAt,
        lastError);
  }
}
