package com.aurafit.avatar.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarJpaRepository extends JpaRepository<AvatarEntity, UUID> {
  Optional<AvatarEntity> findTopByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);
}
