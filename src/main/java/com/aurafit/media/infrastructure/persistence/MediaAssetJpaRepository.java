package com.aurafit.media.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetJpaRepository extends JpaRepository<MediaAssetEntity, UUID> {}
