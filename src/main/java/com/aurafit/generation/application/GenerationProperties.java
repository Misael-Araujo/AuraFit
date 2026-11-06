package com.aurafit.generation.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aurafit.generation")
public record GenerationProperties(
    @Positive int maxConcurrentJobs,
    @Positive int dailyLimit,
    @Positive int maxAttempts,
    @NotNull Duration dailyQuotaWindow,
    @NotNull Duration leaseDuration,
    @NotNull Duration backoffBase,
    @NotNull Duration workerDelay) {
  public GenerationProperties {
    if (maxConcurrentJobs <= 0) {
      throw new IllegalArgumentException("Generation maxConcurrentJobs must be positive.");
    }
    if (dailyLimit <= 0) {
      throw new IllegalArgumentException("Generation dailyLimit must be positive.");
    }
    if (maxAttempts <= 0) {
      throw new IllegalArgumentException("Generation maxAttempts must be positive.");
    }
    if (dailyQuotaWindow == null || dailyQuotaWindow.isZero() || dailyQuotaWindow.isNegative()) {
      throw new IllegalArgumentException("Generation dailyQuotaWindow must be positive.");
    }
    if (leaseDuration == null || leaseDuration.isZero() || leaseDuration.isNegative()) {
      throw new IllegalArgumentException("Generation leaseDuration must be positive.");
    }
    if (backoffBase == null || backoffBase.isZero() || backoffBase.isNegative()) {
      throw new IllegalArgumentException("Generation backoffBase must be positive.");
    }
    if (workerDelay == null || workerDelay.isZero() || workerDelay.isNegative()) {
      throw new IllegalArgumentException("Generation workerDelay must be positive.");
    }
  }
}
