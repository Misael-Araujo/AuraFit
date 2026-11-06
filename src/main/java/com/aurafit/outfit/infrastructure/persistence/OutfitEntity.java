package com.aurafit.outfit.infrastructure.persistence;

import com.aurafit.outfit.domain.Outfit;
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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "outfit")
public class OutfitEntity {
  @Id private UUID id;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(name = "name", nullable = false, length = 128)
  private String name;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "outfit_item", joinColumns = @JoinColumn(name = "outfit_id"))
  @OrderColumn(name = "item_order")
  private List<OutfitItemEmbeddable> items = new ArrayList<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "outfit_season", joinColumns = @JoinColumn(name = "outfit_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "season")
  private Set<SeasonTag> seasons = new LinkedHashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "outfit_occasion", joinColumns = @JoinColumn(name = "outfit_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "occasion")
  private Set<OccasionTag> occasions = new LinkedHashSet<>();

  @Column(name = "favorite", nullable = false)
  private boolean favorite;

  @Column(name = "current_render_media_id")
  private UUID currentRenderMediaId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  protected OutfitEntity() {}

  public static OutfitEntity fromAggregate(Outfit outfit) {
    OutfitEntity entity = new OutfitEntity();
    entity.id = outfit.id();
    entity.ownerUserId = outfit.ownerUserId();
    entity.name = outfit.name();
    entity.items =
        outfit.items() == null
            ? new ArrayList<>()
            : outfit.items().stream().map(OutfitItemEmbeddable::fromAggregate).toList();
    entity.seasons =
        outfit.seasons() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(outfit.seasons());
    entity.occasions =
        outfit.occasions() == null
            ? new LinkedHashSet<>()
            : new LinkedHashSet<>(outfit.occasions());
    entity.favorite = outfit.favorite();
    entity.currentRenderMediaId = outfit.currentRenderMediaId();
    entity.createdAt = outfit.createdAt();
    entity.updatedAt = outfit.updatedAt();
    entity.version = outfit.version();
    return entity;
  }

  public Outfit toAggregate() {
    return new Outfit(
        id,
        ownerUserId,
        name,
        items == null ? List.of() : items.stream().map(OutfitItemEmbeddable::toAggregate).toList(),
        seasons == null ? Set.of() : Set.copyOf(seasons),
        occasions == null ? Set.of() : Set.copyOf(occasions),
        favorite,
        currentRenderMediaId,
        createdAt,
        updatedAt,
        version);
  }
}
