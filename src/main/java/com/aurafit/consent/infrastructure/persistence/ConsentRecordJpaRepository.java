package com.aurafit.consent.infrastructure.persistence;

import com.aurafit.consent.domain.ConsentPurpose;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ConsentRecordJpaRepository extends JpaRepository<ConsentRecordEntity, UUID> {

  List<ConsentRecordEntity> findByUserIdOrderByRecordedAtDesc(UUID userId);

  Optional<ConsentRecordEntity> findTopByUserIdAndPurposeOrderByRecordedAtDesc(
      UUID userId, ConsentPurpose purpose);
}
