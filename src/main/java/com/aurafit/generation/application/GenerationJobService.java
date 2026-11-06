package com.aurafit.generation.application;

import com.aurafit.generation.domain.GenerationJob;
import com.aurafit.generation.domain.GenerationJobState;
import com.aurafit.shared.error.ApiException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenerationJobService {
  private final GenerationJobRepositoryPort repository;
  private final GenerationProperties properties;

  public GenerationJobService(
      GenerationJobRepositoryPort repository, GenerationProperties properties) {
    this.repository = repository;
    this.properties = properties;
  }

  @Transactional
  public GenerationJob submit(UUID userId, String jobType, String payload, String idempotencyKey) {
    if (jobType == null || jobType.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_job_type", "A job type is required.");
    }
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      return repository
          .findByUserIdAndIdempotencyKey(userId, idempotencyKey)
          .orElseGet(() -> create(jobType, payload, userId, idempotencyKey));
    }
    return create(jobType, payload, userId, UUID.randomUUID().toString());
  }

  @Transactional(readOnly = true)
  public GenerationJob findOwned(UUID userId, UUID jobId) {
    GenerationJob job =
        repository
            .findById(jobId)
            .orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "job_not_found", "Job not found."));
    if (!job.userId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "job_forbidden", "You do not own this job.");
    }
    return job;
  }

  @Transactional(readOnly = true)
  public Page<GenerationJob> listOwned(UUID userId, Pageable pageable) {
    return repository.findByUserId(userId, pageable);
  }

  @Transactional
  public GenerationJob cancelOwned(UUID userId, UUID jobId) {
    GenerationJob job = findOwned(userId, jobId);
    if (job.status() == GenerationJobState.COMPLETED
        || job.status() == GenerationJobState.FAILED
        || job.status() == GenerationJobState.CANCELED
        || job.status() == GenerationJobState.DEAD_LETTER) {
      return job;
    }
    Instant now = Instant.now();
    GenerationJob updated =
        new GenerationJob(
            job.id(),
            job.userId(),
            job.jobType(),
            job.payload(),
            job.idempotencyKey(),
            GenerationJobState.CANCELED,
            job.attemptCount(),
            job.maxAttempts(),
            job.createdAt(),
            now,
            job.scheduledFor(),
            null,
            now,
            "Canceled by user.");
    return repository.save(updated);
  }

  @Transactional(readOnly = true)
  public List<GenerationJob> claimRunnable(int limit) {
    return repository.findRunnable(Instant.now(), limit);
  }

  public long queueDepth() {
    return repository.countQueued();
  }

  private GenerationJob create(String jobType, String payload, UUID userId, String idempotencyKey) {
    Instant now = Instant.now();
    long activeJobs =
        repository.countByUserIdAndStatusIn(
            userId,
            List.of(
                GenerationJobState.PENDING,
                GenerationJobState.PROCESSING,
                GenerationJobState.CANCELED));
    if (activeJobs >= properties.maxConcurrentJobs()) {
      throw new ApiException(
          HttpStatus.TOO_MANY_REQUESTS,
          "job_quota_exceeded",
          "You have reached the maximum number of active jobs.");
    }
    long createdToday =
        repository.countByUserIdAndCreatedAtAfter(userId, now.minus(properties.dailyQuotaWindow()));
    if (createdToday >= properties.dailyLimit()) {
      throw new ApiException(
          HttpStatus.TOO_MANY_REQUESTS,
          "daily_job_quota_exceeded",
          "You have reached the daily generation quota.");
    }
    GenerationJob job =
        new GenerationJob(
            UUID.randomUUID(),
            userId,
            jobType,
            payload,
            idempotencyKey,
            GenerationJobState.PENDING,
            0,
            properties.maxAttempts(),
            now,
            now,
            now,
            null,
            null,
            null);
    return repository.save(job);
  }
}
