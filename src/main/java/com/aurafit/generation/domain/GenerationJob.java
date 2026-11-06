package com.aurafit.generation.domain;

import java.time.Instant;
import java.util.UUID;

public record GenerationJob(
    UUID id,
    UUID userId,
    String jobType,
    String payload,
    String idempotencyKey,
    GenerationJobState status,
    int attemptCount,
    int maxAttempts,
    Instant createdAt,
    Instant updatedAt,
    Instant scheduledFor,
    Instant leaseUntil,
    Instant completedAt,
    String lastError) {}
