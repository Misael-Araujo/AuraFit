package com.aurafit.generation.application;

import com.aurafit.generation.domain.GenerationJob;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobQueuePort {
  UUID submit(UUID userId, String jobType, String payload, String idempotencyKey);

  GenerationJob getOwned(UUID userId, UUID jobId);

  Page<GenerationJob> listOwned(UUID userId, Pageable pageable);

  GenerationJob cancelOwned(UUID userId, UUID jobId);

  long queueDepth();
}
