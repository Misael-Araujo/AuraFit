package com.aurafit.generation.infrastructure.queue;

import com.aurafit.generation.application.GenerationJobService;
import com.aurafit.generation.application.JobQueuePort;
import com.aurafit.generation.domain.GenerationJob;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class DbGenerationJobQueue implements JobQueuePort {
  private final GenerationJobService service;

  public DbGenerationJobQueue(GenerationJobService service) {
    this.service = service;
  }

  @Override
  public UUID submit(UUID userId, String jobType, String payload, String idempotencyKey) {
    return service.submit(userId, jobType, payload, idempotencyKey).id();
  }

  @Override
  public GenerationJob getOwned(UUID userId, UUID jobId) {
    return service.findOwned(userId, jobId);
  }

  @Override
  public Page<GenerationJob> listOwned(UUID userId, Pageable pageable) {
    return service.listOwned(userId, pageable);
  }

  @Override
  public GenerationJob cancelOwned(UUID userId, UUID jobId) {
    return service.cancelOwned(userId, jobId);
  }

  @Override
  public long queueDepth() {
    return service.queueDepth();
  }
}
