package com.aurafit.outfit.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OutfitJpaRepository
    extends JpaRepository<OutfitEntity, UUID>, JpaSpecificationExecutor<OutfitEntity> {}
