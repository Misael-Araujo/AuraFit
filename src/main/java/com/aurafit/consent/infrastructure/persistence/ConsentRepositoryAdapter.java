package com.aurafit.consent.infrastructure.persistence;

import com.aurafit.consent.application.ConsentRepositoryPort;
import com.aurafit.consent.domain.ConsentPurpose;
import com.aurafit.consent.domain.ConsentRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class ConsentRepositoryAdapter implements ConsentRepositoryPort {

  private final ConsentRecordJpaRepository repository;

  ConsentRepositoryAdapter(ConsentRecordJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public ConsentRecord save(ConsentRecord record) {
    return repository.save(ConsentRecordEntity.from(record)).toDomain();
  }

  @Override
  public List<ConsentRecord> findByUserId(UUID userId) {
    return repository.findByUserIdOrderByRecordedAtDesc(userId).stream()
        .map(ConsentRecordEntity::toDomain)
        .toList();
  }

  @Override
  public Optional<ConsentRecord> findLatestByUserIdAndPurpose(UUID userId, ConsentPurpose purpose) {
    return repository
        .findTopByUserIdAndPurposeOrderByRecordedAtDesc(userId, purpose)
        .map(ConsentRecordEntity::toDomain);
  }
}
