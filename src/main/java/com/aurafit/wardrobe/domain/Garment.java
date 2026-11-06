package com.aurafit.wardrobe.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record Garment(
    UUID id,
    UUID ownerUserId,
    GarmentSource source,
    GarmentStatus status,
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
    long version) {}
