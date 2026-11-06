package com.aurafit.wardrobe.application;

import com.aurafit.wardrobe.domain.OccasionTag;
import com.aurafit.wardrobe.domain.SeasonTag;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record GarmentExtractionResult(
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
    BigDecimal price,
    UUID digitalGarmentMediaId) {}
