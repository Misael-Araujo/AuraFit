package com.aurafit.generation.application;

import com.aurafit.generation.domain.GenerationJob;
import com.aurafit.generation.domain.GenerationJobState;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenerationJobRepositoryPort {
  GenerationJob save(GenerationJob job);

  Optional<GenerationJob> findById(UUID id);

  Optional<GenerationJob> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

  Page<GenerationJob> findByUserId(UUID userId, Pageable pageable);

  List<GenerationJob> findRunnable(Instant now, int limit);

  long countByUserIdAndStatusIn(UUID userId, List<GenerationJobState> states);

  long countByUserIdAndCreatedAtAfter(UUID userId, Instant after);

  long countQueued();
}
