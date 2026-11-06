package com.aurafit.wardrobe.web;

import com.aurafit.wardrobe.domain.Garment;
import com.aurafit.wardrobe.domain.OccasionTag;
import com.aurafit.wardrobe.domain.SeasonTag;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record GarmentView(
    UUID id,
    UUID ownerUserId,
    String source,
    String status,
    String category,
    String subcategory,
    List<String> colors,
    String colorFamily,
    String pattern,
    String material,
    String brand,
    String size,
    Set<SeasonTag> seasons,
    Set<OccasionTag> occasions,
    String notes,
    String purchaseUrl,
    BigDecimal price,
    List<UUID> originalMediaIds,
    UUID digitalGarmentMediaId,
    Instant createdAt,
    Instant updatedAt,
    long version) {

  public static GarmentView of(Garment garment) {
    return new GarmentView(
        garment.id(),
        garment.ownerUserId(),
        garment.source().name(),
        garment.status().name(),
        garment.category(),
        garment.subcategory(),
        garment.colors(),
        garment.colorFamily(),
        garment.pattern(),
        garment.material(),
        garment.brand(),
        garment.size(),
        garment.seasons(),
        garment.occasions(),
        garment.notes(),
        garment.purchaseUrl(),
        garment.price(),
        garment.originalMediaIds(),
        garment.digitalGarmentMediaId(),
        garment.createdAt(),
        garment.updatedAt(),
        garment.version());
  }
}
