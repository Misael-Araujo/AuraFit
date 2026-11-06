package com.aurafit.outfit.infrastructure.persistence;

import com.aurafit.outfit.domain.OutfitItem;
import com.aurafit.outfit.domain.OutfitSlot;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;

@Embeddable
public class OutfitItemEmbeddable {
  @Column(name = "garment_id", nullable = false)
  private UUID garmentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "slot", nullable = false, length = 16)
  private OutfitSlot slot;

  protected OutfitItemEmbeddable() {}

  public OutfitItemEmbeddable(UUID garmentId, OutfitSlot slot) {
    this.garmentId = garmentId;
    this.slot = slot;
  }

  public static OutfitItemEmbeddable fromAggregate(OutfitItem item) {
    return new OutfitItemEmbeddable(item.garmentId(), item.slot());
  }

  public OutfitItem toAggregate() {
    return new OutfitItem(garmentId, slot);
  }
}
