package com.aurafit.generation.infrastructure.persistence;

import com.aurafit.generation.domain.GenerationJobState;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GenerationJobJpaRepository extends JpaRepository<GenerationJobEntity, UUID> {
  Optional<GenerationJobEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

  Page<GenerationJobEntity> findByUserId(UUID userId, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select j from GenerationJobEntity j "
          + "where j.status in :statuses "
          + "and (j.leaseUntil is null or j.leaseUntil <= :now) "
          + "and j.scheduledFor <= :now order by j.createdAt asc")
  List<GenerationJobEntity> findRunnable(
      @Param("statuses") List<GenerationJobState> statuses,
      @Param("now") Instant now,
      Pageable pageable);

  long countByUserIdAndStatusIn(UUID userId, List<GenerationJobState> states);

  long countByUserIdAndCreatedAtAfter(UUID userId, Instant after);

  long countByStatus(GenerationJobState status);
}
