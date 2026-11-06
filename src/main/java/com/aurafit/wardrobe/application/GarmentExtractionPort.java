package com.aurafit.wardrobe.application;

import com.aurafit.wardrobe.domain.GarmentSource;
import java.util.List;
import java.util.UUID;

public interface GarmentExtractionPort {
  GarmentExtractionResult extract(UUID userId, List<UUID> originalMediaIds, GarmentSource source);
}
