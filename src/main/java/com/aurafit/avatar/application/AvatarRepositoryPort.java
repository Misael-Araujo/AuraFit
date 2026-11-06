package com.aurafit.avatar.application;

import com.aurafit.avatar.domain.Avatar;
import java.util.Optional;
import java.util.UUID;

public interface AvatarRepositoryPort {
  Avatar save(Avatar avatar);

  Optional<Avatar> findById(UUID id);

  Optional<Avatar> findCurrentByUserId(UUID userId);

  Optional<AvatarPublicView> findAvatarViewById(UUID id);

  Optional<AvatarPublicView> findCurrentAvatarView(UUID userId);

  void deleteById(UUID id);
}
