package com.aurafit.outfit.infrastructure.persistence;

import com.aurafit.wardrobe.domain.OccasionTag;
import com.aurafit.wardrobe.domain.SeasonTag;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class OutfitSpecifications {
  private OutfitSpecifications() {}

  public static Specification<OutfitEntity> forUser(
      UUID ownerUserId, com.aurafit.outfit.application.OutfitFilter filter) {
    Specification<OutfitEntity> specification =
        (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("ownerUserId"), ownerUserId);
    if (filter == null) {
      return specification;
    }

    if (filter.favorite() != null && !filter.favorite().isBlank()) {
      boolean favorite = Boolean.parseBoolean(filter.favorite().trim());
      specification =
          specification.and((root, query, cb) -> cb.equal(root.get("favorite"), favorite));
    }
    if (filter.season() != null && !filter.season().isBlank()) {
      SeasonTag season = SeasonTag.valueOf(filter.season().trim().toUpperCase(Locale.ROOT));
      specification =
          specification.and((root, query, cb) -> cb.isMember(season, root.get("seasons")));
    }
    if (filter.occasion() != null && !filter.occasion().isBlank()) {
      OccasionTag occasion = OccasionTag.valueOf(filter.occasion().trim().toUpperCase(Locale.ROOT));
      specification =
          specification.and((root, query, cb) -> cb.isMember(occasion, root.get("occasions")));
    }
    if (filter.freeText() != null && !filter.freeText().isBlank()) {
      String text = "%" + filter.freeText().trim().toLowerCase(Locale.ROOT) + "%";
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("name")), text),
                      cb.like(cb.lower(root.get("name")), text)));
    }
    return specification;
  }
}
