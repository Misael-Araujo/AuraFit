package com.aurafit.media.application;

import com.aurafit.media.domain.MediaAsset;
import java.util.Optional;
import java.util.UUID;

public interface MediaAssetRepositoryPort {
  MediaAsset save(MediaAsset asset);

  Optional<MediaAsset> findById(UUID id);

  void deleteById(UUID id);
}
