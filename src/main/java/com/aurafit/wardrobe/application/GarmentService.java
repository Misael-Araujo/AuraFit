package com.aurafit.wardrobe.application;

import com.aurafit.generation.application.JobQueuePort;
import com.aurafit.media.application.MediaService;
import com.aurafit.media.domain.MediaAsset;
import com.aurafit.shared.error.ApiException;
import com.aurafit.wardrobe.domain.Garment;
import com.aurafit.wardrobe.domain.GarmentSource;
import com.aurafit.wardrobe.domain.GarmentStatus;
import com.aurafit.wardrobe.domain.OccasionTag;
import com.aurafit.wardrobe.domain.SeasonTag;
import com.aurafit.wardrobe.web.GarmentPatchRequest;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GarmentService {
  private final GarmentRepositoryPort garments;
  private final JobQueuePort jobQueuePort;
  private final MediaService mediaService;
  private final GarmentExtractionPort extractionPort;

  public GarmentService(
      GarmentRepositoryPort garments,
      JobQueuePort jobQueuePort,
      MediaService mediaService,
      GarmentExtractionPort extractionPort) {
    this.garments = garments;
    this.jobQueuePort = jobQueuePort;
    this.mediaService = mediaService;
    this.extractionPort = extractionPort;
  }

  @Transactional
  public UUID scan(
      UUID userId, MultipartFile[] files, GarmentSource source, String idempotencyKey) {
    if (files == null || files.length == 0 || files.length > 3) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "garment_scan_invalid_files",
          "Between 1 and 3 garment images are required.");
    }
    if (source == null) {
      source = GarmentSource.OWNED;
    }

    List<UUID> mediaIds = new ArrayList<>();
    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) {
        throw new ApiException(
            HttpStatus.BAD_REQUEST,
            "garment_scan_empty_file",
            "Each uploaded garment image must contain content.");
      }
      MediaAsset uploaded = mediaService.upload(userId, file);
      mediaIds.add(uploaded.id());
    }

    Instant now = Instant.now();
    Garment garment =
        new Garment(
            UUID.randomUUID(),
            userId,
            source,
            GarmentStatus.PROCESSING,
            "UNSPECIFIED",
            null,
            List.of(),
            null,
            null,
            null,
            null,
            null,
            Set.of(),
            Set.of(),
            null,
            null,
            null,
            mediaIds,
            null,
            now,
            now,
            0L);
    garments.save(garment);

    String payload = String.join(",", mediaIds.stream().map(UUID::toString).toList());
    String key =
        (idempotencyKey == null || idempotencyKey.isBlank())
            ? "garment:" + garment.id()
            : idempotencyKey;
    return jobQueuePort.submit(userId, "garment_extraction", payload, key);
  }

  @Transactional(readOnly = true)
  public Garment getOwned(UUID userId, UUID garmentId) {
    Garment garment =
        garments
            .findById(garmentId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND, "garment_not_found", "Garment not found."));
    if (!Objects.equals(garment.ownerUserId(), userId)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "garment_forbidden", "You do not own this garment.");
    }
    return garment;
  }

  @Transactional(readOnly = true)
  public Page<Garment> listOwned(UUID userId, GarmentFilter filter, Pageable pageable) {
    return garments.findOwned(userId, filter, pageable);
  }

  @Transactional
  public Garment updateOwned(
      UUID userId, UUID garmentId, GarmentPatchRequest patch, long expectedVersion) {
    Garment existing = getOwned(userId, garmentId);
    if (existing.version() != expectedVersion) {
      throw new ApiException(
          HttpStatus.PRECONDITION_FAILED,
          "garment_version_mismatch",
          "The garment has changed since the supplied ETag was generated.");
    }
    if (patch == null) {
      return existing;
    }

    String purchaseUrl =
        patch.purchaseUrl() == null
            ? existing.purchaseUrl()
            : validatePurchaseUrl(patch.purchaseUrl(), existing.source());
    BigDecimal price = patch.price() == null ? existing.price() : patch.price();
    List<String> colors =
        patch.colors() == null ? existing.colors() : sanitizeColors(patch.colors());
    String colorFamily = patch.colorFamily() == null ? existing.colorFamily() : patch.colorFamily();
    String category = patch.category() == null ? existing.category() : patch.category();
    String subcategory = patch.subcategory() == null ? existing.subcategory() : patch.subcategory();
    String pattern = patch.pattern() == null ? existing.pattern() : patch.pattern();
    String material = patch.material() == null ? existing.material() : patch.material();
    String brand = patch.brand() == null ? existing.brand() : patch.brand();
    String size = patch.size() == null ? existing.size() : patch.size();
    String notes = patch.notes() == null ? existing.notes() : patch.notes();
    Set<SeasonTag> seasons = patch.seasons() == null ? existing.seasons() : patch.seasons();
    Set<OccasionTag> occasions =
        patch.occasions() == null ? existing.occasions() : patch.occasions();

    Garment updated =
        new Garment(
            existing.id(),
            existing.ownerUserId(),
            existing.source(),
            existing.status(),
            category,
            subcategory,
            colors,
            colorFamily,
            pattern,
            material,
            brand,
            size,
            seasons,
            occasions,
            notes,
            purchaseUrl,
            price,
            existing.originalMediaIds(),
            existing.digitalGarmentMediaId(),
            existing.createdAt(),
            Instant.now(),
            existing.version() + 1L);
    return garments.save(updated);
  }

  @Transactional
  public void deleteOwned(UUID userId, UUID garmentId) {
    Garment garment = getOwned(userId, garmentId);
    garments.deleteById(garment.id());
  }

  @Transactional
  public Garment completeExtraction(UUID userId, UUID garmentId) {
    Garment garment = getOwned(userId, garmentId);
    GarmentExtractionResult result =
        extractionPort.extract(userId, garment.originalMediaIds(), garment.source());
    Garment updated =
        new Garment(
            garment.id(),
            garment.ownerUserId(),
            garment.source(),
            GarmentStatus.READY,
            result.category(),
            result.subcategory(),
            result.colors(),
            result.colorFamily(),
            result.pattern(),
            result.material(),
            result.brand(),
            result.size(),
            result.seasons(),
            result.occasions(),
            result.notes(),
            garment.purchaseUrl(),
            result.price(),
            garment.originalMediaIds(),
            result.digitalGarmentMediaId(),
            garment.createdAt(),
            Instant.now(),
            garment.version() + 1L);
    return garments.save(updated);
  }

  private String validatePurchaseUrl(String purchaseUrl, GarmentSource source) {
    if (source != GarmentSource.WISHLIST && purchaseUrl != null) {
      return purchaseUrl;
    }
    if (purchaseUrl == null || purchaseUrl.isBlank()) {
      return null;
    }
    URI uri = URI.create(purchaseUrl);
    if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "invalid_purchase_url", "Purchase URLs must use http or https.");
    }
    if (uri.getHost() == null || uri.getHost().isBlank()) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "invalid_purchase_url",
          "Purchase URLs must include a valid host.");
    }
    return purchaseUrl;
  }

  private List<String> sanitizeColors(List<String> rawColors) {
    if (rawColors == null || rawColors.isEmpty()) {
      return Collections.emptyList();
    }
    Set<String> colors = new LinkedHashSet<>();
    for (String color : rawColors) {
      if (color == null || color.isBlank()) {
        continue;
      }
      colors.add(color.trim());
    }
    return List.copyOf(colors);
  }
}
