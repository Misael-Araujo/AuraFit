package com.aurafit.outfit.application;

import com.aurafit.avatar.application.AvatarPublicView;
import com.aurafit.avatar.application.AvatarRepositoryPort;
import com.aurafit.generation.application.JobQueuePort;
import com.aurafit.outfit.domain.Outfit;
import com.aurafit.outfit.domain.OutfitItem;
import com.aurafit.outfit.web.OutfitPatchRequest;
import com.aurafit.shared.error.ApiException;
import com.aurafit.wardrobe.application.GarmentRepositoryPort;
import com.aurafit.wardrobe.domain.Garment;
import com.aurafit.wardrobe.domain.GarmentStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutfitService {
  private final OutfitRepositoryPort outfits;
  private final GarmentRepositoryPort garments;
  private final AvatarRepositoryPort avatars;
  private final JobQueuePort jobQueuePort;
  private final TryOnRenderPort tryOnRenderPort;

  public OutfitService(
      OutfitRepositoryPort outfits,
      GarmentRepositoryPort garments,
      AvatarRepositoryPort avatars,
      JobQueuePort jobQueuePort,
      TryOnRenderPort tryOnRenderPort) {
    this.outfits = outfits;
    this.garments = garments;
    this.avatars = avatars;
    this.jobQueuePort = jobQueuePort;
    this.tryOnRenderPort = tryOnRenderPort;
  }

  @Transactional
  public Outfit create(UUID userId, String name, List<OutfitItem> items, boolean favorite) {
    if (items == null || items.isEmpty()) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "outfit_items_required",
          "An outfit must include at least one item.");
    }
    OutfitPolicy.validate(items);
    validateGarmentMembership(userId, items);

    Instant now = Instant.now();
    Outfit outfit =
        new Outfit(
            UUID.randomUUID(),
            userId,
            normalizeName(name),
            new ArrayList<>(items),
            Set.of(),
            Set.of(),
            favorite,
            null,
            now,
            now,
            0L);
    return outfits.save(outfit);
  }

  @Transactional(readOnly = true)
  public Page<Outfit> listOwned(UUID userId, OutfitFilter filter, Pageable pageable) {
    return outfits.findOwned(userId, filter, pageable);
  }

  @Transactional(readOnly = true)
  public Outfit getOwned(UUID userId, UUID outfitId) {
    Outfit outfit =
        outfits
            .findById(outfitId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND, "outfit_not_found", "Outfit not found."));
    if (!Objects.equals(outfit.ownerUserId(), userId)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "outfit_forbidden", "You do not own this outfit.");
    }
    return outfit;
  }

  @Transactional
  public Outfit updateOwned(
      UUID userId, UUID outfitId, String name, List<OutfitItem> items, boolean favorite) {
    Outfit existing = getOwned(userId, outfitId);
    if (items == null || items.isEmpty()) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "outfit_items_required",
          "An outfit must include at least one item.");
    }
    OutfitPolicy.validate(items);
    validateGarmentMembership(userId, items);

    Outfit updated =
        new Outfit(
            existing.id(),
            existing.ownerUserId(),
            normalizeName(name),
            new ArrayList<>(items),
            existing.seasons(),
            existing.occasions(),
            favorite,
            existing.currentRenderMediaId(),
            existing.createdAt(),
            Instant.now(),
            existing.version() + 1L);
    return outfits.save(updated);
  }

  @Transactional
  public Outfit updateOwned(
      UUID userId, UUID outfitId, OutfitPatchRequest patch, long expectedVersion) {
    Outfit existing = getOwned(userId, outfitId);
    if (existing.version() != expectedVersion) {
      throw new ApiException(
          HttpStatus.PRECONDITION_FAILED,
          "outfit_version_mismatch",
          "The outfit has changed since the supplied ETag was generated.");
    }
    if (patch == null) {
      return existing;
    }

    List<OutfitItem> items =
        patch.items() == null ? existing.items() : new ArrayList<>(patch.items());
    String name = patch.name() == null ? existing.name() : normalizeName(patch.name());
    boolean favorite = patch.favorite() == null ? existing.favorite() : patch.favorite();
    if (items == null || items.isEmpty()) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "outfit_items_required",
          "An outfit must include at least one item.");
    }
    OutfitPolicy.validate(items);
    validateGarmentMembership(userId, items);

    Outfit updated =
        new Outfit(
            existing.id(),
            existing.ownerUserId(),
            name,
            items,
            existing.seasons(),
            existing.occasions(),
            favorite,
            existing.currentRenderMediaId(),
            existing.createdAt(),
            Instant.now(),
            existing.version() + 1L);
    return outfits.save(updated);
  }

  @Transactional
  public void deleteOwned(UUID userId, UUID outfitId) {
    getOwned(userId, outfitId);
    outfits.deleteById(outfitId);
  }

  @Transactional
  public Outfit toggleFavorite(UUID userId, UUID outfitId, boolean favoriteToSet) {
    Outfit existing = getOwned(userId, outfitId);
    Outfit updated =
        new Outfit(
            existing.id(),
            existing.ownerUserId(),
            existing.name(),
            existing.items(),
            existing.seasons(),
            existing.occasions(),
            favoriteToSet,
            existing.currentRenderMediaId(),
            existing.createdAt(),
            Instant.now(),
            existing.version() + 1L);
    return outfits.save(updated);
  }

  @Transactional
  public UUID renderOwned(UUID userId, UUID outfitId) {
    Outfit outfit = getOwned(userId, outfitId);
    AvatarPublicView avatar =
        avatars
            .findCurrentAvatarView(userId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.FORBIDDEN,
                        "avatar_required",
                        "An active avatar is required to render an outfit."));
    if (!"ACTIVE".equalsIgnoreCase(avatar.status())) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "avatar_inactive", "The user's avatar is not active yet.");
    }
    List<UUID> garmentIds =
        outfit.items().stream()
            .map(OutfitItem::garmentId)
            .sorted(Comparator.naturalOrder())
            .toList();
    String renderKey =
        avatar.id()
            + ":"
            + avatar.version()
            + ":"
            + String.join("|", garmentIds.stream().map(UUID::toString).toList());
    String payload = renderKey;
    return jobQueuePort.submit(userId, "tryon_render", payload, "tryon:" + renderKey);
  }

  @Transactional
  public UUID renderAdHoc(UUID userId, List<OutfitItem> items, UUID avatarId) {
    OutfitPolicy.validate(items);
    validateGarmentMembership(userId, items);
    AvatarPublicView avatar =
        avatars
            .findAvatarViewById(avatarId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND, "avatar_not_found", "Avatar not found."));
    if (!Objects.equals(avatar.ownerUserId(), userId)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "avatar_forbidden", "You do not own this avatar.");
    }
    if (!"ACTIVE".equalsIgnoreCase(avatar.status())) {
      throw new ApiException(
          HttpStatus.FORBIDDEN,
          "avatar_inactive",
          "The avatar must be active before trying on a combination.");
    }
    List<UUID> garmentIds =
        items.stream().map(OutfitItem::garmentId).sorted(Comparator.naturalOrder()).toList();
    String renderKey =
        avatar.id()
            + ":"
            + avatar.version()
            + ":"
            + String.join("|", garmentIds.stream().map(UUID::toString).toList());
    return jobQueuePort.submit(userId, "tryon_render", renderKey, "tryon:adhoc:" + renderKey);
  }

  private void validateGarmentMembership(UUID userId, List<OutfitItem> items) {
    for (OutfitItem item : items) {
      Garment garment =
          garments
              .findById(item.garmentId())
              .orElseThrow(
                  () ->
                      new ApiException(
                          HttpStatus.NOT_FOUND,
                          "garment_not_found",
                          "Referenced garment not found."));
      if (!Objects.equals(garment.ownerUserId(), userId)) {
        throw new ApiException(
            HttpStatus.FORBIDDEN,
            "garment_forbidden",
            "You do not own the garment referenced by this outfit.");
      }
      if (garment.status() != GarmentStatus.READY) {
        throw new ApiException(
            HttpStatus.BAD_REQUEST,
            "garment_not_ready",
            "Garments must be READY before they can be used in an outfit.");
      }
    }
  }

  private String normalizeName(String name) {
    if (name == null || name.isBlank()) {
      return "My outfit";
    }
    return name.trim();
  }
}
