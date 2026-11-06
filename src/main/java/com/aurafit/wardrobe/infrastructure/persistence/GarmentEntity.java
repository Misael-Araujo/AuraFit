package com.aurafit.wardrobe.infrastructure.persistence;

import com.aurafit.wardrobe.domain.Garment;
import com.aurafit.wardrobe.domain.GarmentSource;
import com.aurafit.wardrobe.domain.GarmentStatus;
import com.aurafit.wardrobe.domain.OccasionTag;
import com.aurafit.wardrobe.domain.SeasonTag;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "garment")
public class GarmentEntity {
  @Id private UUID id;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false)
  private GarmentSource source;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private GarmentStatus status;

  @Column(name = "category", length = 64)
  private String category;

  @Column(name = "subcategory", length = 64)
  private String subcategory;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "garment_color", joinColumns = @JoinColumn(name = "garment_id"))
  @Column(name = "color_value", length = 64)
  private List<String> colors = new ArrayList<>();

  @Column(name = "color_family", length = 64)
  private String colorFamily;

  @Column(name = "pattern", length = 128)
  private String pattern;

  @Column(name = "material", length = 128)
  private String material;

  @Column(name = "brand", length = 128)
  private String brand;

  @Column(name = "size", length = 32)
  private String size;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "garment_season", joinColumns = @JoinColumn(name = "garment_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "season")
  private Set<SeasonTag> seasons = new LinkedHashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "garment_occasion", joinColumns = @JoinColumn(name = "garment_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "occasion")
  private Set<OccasionTag> occasions = new LinkedHashSet<>();

  @Column(name = "notes", length = 2048)
  private String notes;

  @Column(name = "purchase_url", length = 2048)
  private String purchaseUrl;

  @Column(name = "price", precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "original_media_ids", length = 2048)
  private String originalMediaIds;

  @Column(name = "digital_garment_media_id")
  private UUID digitalGarmentMediaId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  protected GarmentEntity() {}

  public static GarmentEntity fromAggregate(Garment garment) {
    GarmentEntity entity = new GarmentEntity();
    entity.id = garment.id();
    entity.ownerUserId = garment.ownerUserId();
    entity.source = garment.source();
    entity.status = garment.status();
    entity.category = garment.category();
    entity.subcategory = garment.subcategory();
    entity.colors =
        garment.colors() == null ? new ArrayList<>() : new ArrayList<>(garment.colors());
    entity.colorFamily = garment.colorFamily();
    entity.pattern = garment.pattern();
    entity.material = garment.material();
    entity.brand = garment.brand();
    entity.size = garment.size();
    entity.seasons =
        garment.seasons() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(garment.seasons());
    entity.occasions =
        garment.occasions() == null
            ? new LinkedHashSet<>()
            : new LinkedHashSet<>(garment.occasions());
    entity.notes = garment.notes();
    entity.purchaseUrl = garment.purchaseUrl();
    entity.price = garment.price();
    entity.originalMediaIds = serializeMediaIds(garment.originalMediaIds());
    entity.digitalGarmentMediaId = garment.digitalGarmentMediaId();
    entity.createdAt = garment.createdAt();
    entity.updatedAt = garment.updatedAt();
    entity.version = garment.version();
    return entity;
  }

  public Garment toAggregate() {
    return new Garment(
        id,
        ownerUserId,
        source,
        status,
        category,
        subcategory,
        colors == null ? Collections.emptyList() : List.copyOf(colors),
        colorFamily,
        pattern,
        material,
        brand,
        size,
        seasons == null ? Set.of() : Set.copyOf(seasons),
        occasions == null ? Set.of() : Set.copyOf(occasions),
        notes,
        purchaseUrl,
        price,
        deserializeMediaIds(originalMediaIds),
        digitalGarmentMediaId,
        createdAt,
        updatedAt,
        version);
  }

  private static String serializeMediaIds(List<UUID> mediaIds) {
    if (mediaIds == null || mediaIds.isEmpty()) {
      return "";
    }
    return String.join(",", mediaIds.stream().map(UUID::toString).toList());
  }

  private static List<UUID> deserializeMediaIds(String serialized) {
    if (serialized == null || serialized.isBlank()) {
      return List.of();
    }
    return Arrays.stream(serialized.split(","))
        .filter(value -> !value.isBlank())
        .map(UUID::fromString)
        .toList();
  }
}
