package com.aurafit.outfit.domain;

import com.aurafit.wardrobe.domain.OccasionTag;
import com.aurafit.wardrobe.domain.SeasonTag;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record Outfit(
    UUID id,
    UUID ownerUserId,
    String name,
    List<OutfitItem> items,
    Set<SeasonTag> seasons,
    Set<OccasionTag> occasions,
    boolean favorite,
    UUID currentRenderMediaId,
    Instant createdAt,
    Instant updatedAt,
    long version) {}
