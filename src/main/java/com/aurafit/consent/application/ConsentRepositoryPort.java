package com.aurafit.consent.application;

import com.aurafit.consent.domain.ConsentPurpose;
import com.aurafit.consent.domain.ConsentRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRepositoryPort {

  ConsentRecord save(ConsentRecord record);

  List<ConsentRecord> findByUserId(UUID userId);

  Optional<ConsentRecord> findLatestByUserIdAndPurpose(UUID userId, ConsentPurpose purpose);
}
