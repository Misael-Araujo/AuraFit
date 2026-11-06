package com.aurafit.generation.infrastructure.persistence;

import com.aurafit.generation.application.GenerationJobRepositoryPort;
import com.aurafit.generation.domain.GenerationJob;
import com.aurafit.generation.domain.GenerationJobState;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class GenerationJobRepositoryAdapter implements GenerationJobRepositoryPort {
  private final GenerationJobJpaRepository repository;

  public GenerationJobRepositoryAdapter(GenerationJobJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public GenerationJob save(GenerationJob job) {
    return repository.save(GenerationJobEntity.fromAggregate(job)).toAggregate();
  }

  @Override
  public Optional<GenerationJob> findById(UUID id) {
    return repository.findById(id).map(GenerationJobEntity::toAggregate);
  }

  @Override
  public Optional<GenerationJob> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey) {
    return repository
        .findByUserIdAndIdempotencyKey(userId, idempotencyKey)
        .map(GenerationJobEntity::toAggregate);
  }

  @Override
  public Page<GenerationJob> findByUserId(UUID userId, Pageable pageable) {
    return repository.findByUserId(userId, pageable).map(GenerationJobEntity::toAggregate);
  }

  @Override
  public List<GenerationJob> findRunnable(Instant now, int limit) {
    return repository
        .findRunnable(
            List.of(GenerationJobState.PENDING, GenerationJobState.PROCESSING),
            now,
            Pageable.ofSize(limit))
        .stream()
        .map(GenerationJobEntity::toAggregate)
        .toList();
  }

  @Override
  public long countByUserIdAndStatusIn(UUID userId, List<GenerationJobState> states) {
    return repository.countByUserIdAndStatusIn(userId, states);
  }

  @Override
  public long countByUserIdAndCreatedAtAfter(UUID userId, Instant after) {
    return repository.countByUserIdAndCreatedAtAfter(userId, after);
  }

  @Override
  public long countQueued() {
    return repository.countByStatus(GenerationJobState.PENDING);
  }
}
