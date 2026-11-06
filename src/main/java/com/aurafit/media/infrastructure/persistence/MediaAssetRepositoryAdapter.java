package com.aurafit.media.infrastructure.persistence;

import com.aurafit.media.application.MediaAssetRepositoryPort;
import com.aurafit.media.domain.MediaAsset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MediaAssetRepositoryAdapter implements MediaAssetRepositoryPort {

  private final MediaAssetJpaRepository repository;

  public MediaAssetRepositoryAdapter(MediaAssetJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public MediaAsset save(MediaAsset asset) {
    return repository.save(MediaAssetEntity.fromAggregate(asset)).toAggregate();
  }

  @Override
  public Optional<MediaAsset> findById(UUID id) {
    return repository.findById(id).map(MediaAssetEntity::toAggregate);
  }

  @Override
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
