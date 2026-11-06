package com.aurafit.outfit.application;

import com.aurafit.outfit.domain.Outfit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OutfitRepositoryPort {
  Outfit save(Outfit outfit);

  Optional<Outfit> findById(UUID id);

  Page<Outfit> findOwned(UUID ownerUserId, OutfitFilter filter, Pageable pageable);

  void deleteById(UUID id);
}
