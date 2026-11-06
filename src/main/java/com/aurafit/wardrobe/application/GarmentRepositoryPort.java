package com.aurafit.wardrobe.application;

import com.aurafit.wardrobe.domain.Garment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GarmentRepositoryPort {
  Garment save(Garment garment);

  Optional<Garment> findById(UUID id);

  Page<Garment> findOwned(UUID ownerUserId, GarmentFilter filter, Pageable pageable);

  void deleteById(UUID id);
}
