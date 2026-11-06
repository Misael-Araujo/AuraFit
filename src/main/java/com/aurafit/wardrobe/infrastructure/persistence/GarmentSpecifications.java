package com.aurafit.wardrobe.infrastructure.persistence;

import com.aurafit.wardrobe.application.GarmentFilter;
import com.aurafit.wardrobe.domain.GarmentSource;
import com.aurafit.wardrobe.domain.GarmentStatus;
import com.aurafit.wardrobe.domain.OccasionTag;
import com.aurafit.wardrobe.domain.SeasonTag;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class GarmentSpecifications {
  private GarmentSpecifications() {}

  public static Specification<GarmentEntity> forUser(UUID ownerUserId, GarmentFilter filter) {
    Specification<GarmentEntity> specification =
        (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("ownerUserId"), ownerUserId);
    if (filter == null) {
      return specification;
    }

    if (filter.source() != null && !filter.source().isBlank()) {
      GarmentSource source = GarmentSource.valueOf(filter.source().trim().toUpperCase(Locale.ROOT));
      specification = specification.and((root, query, cb) -> cb.equal(root.get("source"), source));
    }
    if (filter.status() != null && !filter.status().isBlank()) {
      GarmentStatus status = GarmentStatus.valueOf(filter.status().trim().toUpperCase(Locale.ROOT));
      specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), status));
    }
    if (filter.category() != null && !filter.category().isBlank()) {
      String category = filter.category().trim();
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.like(
                      cb.lower(root.get("category")),
                      "%" + category.toLowerCase(Locale.ROOT) + "%"));
    }
    if (filter.colorFamily() != null && !filter.colorFamily().isBlank()) {
      String family = filter.colorFamily().trim();
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.like(
                      cb.lower(root.get("colorFamily")),
                      "%" + family.toLowerCase(Locale.ROOT) + "%"));
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
      String freeText = "%" + filter.freeText().trim().toLowerCase(Locale.ROOT) + "%";
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("brand")), freeText),
                      cb.like(cb.lower(root.get("notes")), freeText),
                      cb.like(cb.lower(root.get("category")), freeText),
                      cb.like(cb.lower(root.get("subcategory")), freeText),
                      cb.like(cb.lower(root.get("pattern")), freeText)));
    }
    return specification;
  }
}
