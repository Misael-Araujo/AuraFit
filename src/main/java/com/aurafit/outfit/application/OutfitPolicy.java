package com.aurafit.outfit.application;

import com.aurafit.outfit.domain.OutfitItem;
import com.aurafit.outfit.domain.OutfitSlot;
import com.aurafit.shared.error.ApiException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public final class OutfitPolicy {
  public static final int MAX_ACCESSORIES = 3;

  private OutfitPolicy() {}

  public static void validate(List<OutfitItem> items) {
    if (items == null || items.isEmpty()) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "outfit_items_required",
          "An outfit must include at least one item.");
    }

    Map<OutfitSlot, Integer> counts = new HashMap<>();
    for (OutfitItem item : items) {
      if (item == null) {
        throw new ApiException(
            HttpStatus.BAD_REQUEST,
            "outfit_item_invalid",
            "Each outfit item must include a garment id and slot.");
      }
      if (item.garmentId() == null) {
        throw new ApiException(
            HttpStatus.BAD_REQUEST,
            "outfit_garment_required",
            "Each outfit item must reference a valid garment id.");
      }
      counts.merge(item.slot(), 1, Integer::sum);
    }

    if (counts.getOrDefault(OutfitSlot.DRESS, 0) > 0
        && (counts.getOrDefault(OutfitSlot.TOP, 0) > 0
            || counts.getOrDefault(OutfitSlot.BOTTOM, 0) > 0)) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "outfit_invalid_combination",
          "A dress cannot be combined with separate top and bottom pieces.");
    }

    for (OutfitSlot slot : OutfitSlot.values()) {
      if (slot == OutfitSlot.ACCESSORY) {
        continue;
      }
      if (counts.getOrDefault(slot, 0) > 1) {
        throw new ApiException(
            HttpStatus.BAD_REQUEST,
            "outfit_duplicate_slot",
            "Only one item may occupy each exclusive slot.");
      }
    }

    if (counts.getOrDefault(OutfitSlot.ACCESSORY, 0) > MAX_ACCESSORIES) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "outfit_accessory_limit",
          "An outfit cannot contain more than three accessories.");
    }
  }
}
