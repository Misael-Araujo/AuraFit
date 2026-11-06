package com.aurafit.generation.web;

import com.aurafit.generation.domain.GenerationJob;
import java.time.Instant;
import java.util.UUID;

public record GenerationJobView(
    UUID id,
    UUID userId,
    String jobType,
    String status,
    int attemptCount,
    int maxAttempts,
    Instant createdAt,
    Instant updatedAt,
    Instant completedAt,
    String lastError) {
  public static GenerationJobView of(GenerationJob job) {
    return new GenerationJobView(
        job.id(),
        job.userId(),
        job.jobType(),
        job.status().name(),
        job.attemptCount(),
        job.maxAttempts(),
        job.createdAt(),
        job.updatedAt(),
        job.completedAt(),
        job.lastError());
  }
}
