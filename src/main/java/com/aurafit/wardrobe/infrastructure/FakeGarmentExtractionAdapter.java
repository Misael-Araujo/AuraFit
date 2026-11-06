package com.aurafit.wardrobe.infrastructure;

import com.aurafit.wardrobe.application.GarmentExtractionPort;
import com.aurafit.wardrobe.application.GarmentExtractionResult;
import com.aurafit.wardrobe.domain.GarmentSource;
import com.aurafit.wardrobe.domain.OccasionTag;
import com.aurafit.wardrobe.domain.SeasonTag;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakeGarmentExtractionAdapter implements GarmentExtractionPort {

  @Override
  public GarmentExtractionResult extract(
      UUID userId, List<UUID> originalMediaIds, GarmentSource source) {
    int seed = userId.hashCode() & Integer.MAX_VALUE;
    String category =
        switch (seed % 4) {
          case 0 -> "TOP";
          case 1 -> "BOTTOM";
          case 2 -> "OUTERWEAR";
          default -> "DRESS";
        };
    String subcategory =
        switch (category) {
          case "TOP" -> "T_SHIRT";
          case "BOTTOM" -> "JEANS";
          case "OUTERWEAR" -> "BOMBER";
          default -> "MAXI";
        };
    Set<SeasonTag> seasons = Set.of(SeasonTag.SPRING, SeasonTag.SUMMER);
    Set<OccasionTag> occasions = Set.of(OccasionTag.CASUAL, OccasionTag.TRAVEL);
    List<String> colors = List.of("#0F172A", "#E2E8F0");
    return new GarmentExtractionResult(
        category,
        subcategory,
        colors,
        "NEUTRAL",
        "SOLID",
        "COTTON",
        "AuraFit Studio",
        "M",
        seasons,
        occasions,
        "Automatically extracted from the provided garment scan.",
        BigDecimal.valueOf(89.99),
        UUID.randomUUID());
  }
}
