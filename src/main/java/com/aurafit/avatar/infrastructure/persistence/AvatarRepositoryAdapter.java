package com.aurafit.avatar.infrastructure.persistence;

import com.aurafit.avatar.application.AvatarPublicView;
import com.aurafit.avatar.application.AvatarRepositoryPort;
import com.aurafit.avatar.domain.Avatar;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AvatarRepositoryAdapter implements AvatarRepositoryPort {
  private final AvatarJpaRepository repository;

  public AvatarRepositoryAdapter(AvatarJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public Avatar save(Avatar avatar) {
    return repository.save(AvatarEntity.fromAggregate(avatar)).toAggregate();
  }

  @Override
  public Optional<Avatar> findById(UUID id) {
    return repository.findById(id).map(AvatarEntity::toAggregate);
  }

  @Override
  public Optional<Avatar> findCurrentByUserId(UUID userId) {
    return repository
        .findTopByOwnerUserIdOrderByCreatedAtDesc(userId)
        .map(AvatarEntity::toAggregate);
  }

  @Override
  public Optional<AvatarPublicView> findAvatarViewById(UUID id) {
    return repository.findById(id).map(entity -> toPublicView(entity.toAggregate()));
  }

  @Override
  public Optional<AvatarPublicView> findCurrentAvatarView(UUID userId) {
    return repository
        .findTopByOwnerUserIdOrderByCreatedAtDesc(userId)
        .map(entity -> toPublicView(entity.toAggregate()));
  }

  @Override
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }

  private AvatarPublicView toPublicView(Avatar avatar) {
    return new AvatarPublicView(
        avatar.id(), avatar.ownerUserId(), avatar.status().name(), avatar.version());
  }
}
