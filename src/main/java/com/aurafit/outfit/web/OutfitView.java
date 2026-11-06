package com.aurafit.outfit.web;

import com.aurafit.outfit.domain.Outfit;
import com.aurafit.outfit.domain.OutfitItem;
import com.aurafit.wardrobe.domain.OccasionTag;
import com.aurafit.wardrobe.domain.SeasonTag;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record OutfitView(
    UUID id,
    UUID ownerUserId,
    String name,
    List<OutfitItem> items,
    Set<SeasonTag> seasons,
    Set<OccasionTag> occasions,
    boolean favorite,
    UUID currentRenderMediaId,
    long version) {
  public static OutfitView of(Outfit outfit) {
    return new OutfitView(
        outfit.id(),
        outfit.ownerUserId(),
        outfit.name(),
        outfit.items(),
        outfit.seasons(),
        outfit.occasions(),
        outfit.favorite(),
        outfit.currentRenderMediaId(),
        outfit.version());
  }
}
