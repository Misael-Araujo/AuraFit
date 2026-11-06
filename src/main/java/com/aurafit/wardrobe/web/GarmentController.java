package com.aurafit.wardrobe.web;

import com.aurafit.shared.error.ApiException;
import com.aurafit.wardrobe.application.GarmentFilter;
import com.aurafit.wardrobe.application.GarmentService;
import com.aurafit.wardrobe.domain.Garment;
import com.aurafit.wardrobe.domain.GarmentSource;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/garments")
@SecurityRequirement(name = "bearerAuth")
public class GarmentController {
  private final GarmentService service;

  public GarmentController(GarmentService service) {
    this.service = service;
  }

  @PostMapping(value = "/scan", consumes = "multipart/form-data")
  public ResponseEntity<Void> scan(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(value = "source", defaultValue = "OWNED") String source,
      @RequestPart(value = "files", required = false) MultipartFile[] files,
      @RequestParam(value = "idempotencyKey", required = false) String idempotencyKey) {
    UUID userId = UUID.fromString(jwt.getSubject());
    UUID jobId = service.scan(userId, files, parseSource(source), idempotencyKey);
    return ResponseEntity.accepted().location(URI.create("/api/v1/jobs/" + jobId)).build();
  }

  @GetMapping
  public Page<GarmentView> list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(required = false) String source,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String colorFamily,
      @RequestParam(required = false) String season,
      @RequestParam(required = false) String occasion,
      @RequestParam(required = false) String freeText,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESC") String sortDirection) {
    UUID userId = UUID.fromString(jwt.getSubject());
    Sort.Direction direction =
        "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    return service
        .listOwned(
            userId,
            new GarmentFilter(
                source,
                status,
                category,
                colorFamily,
                season,
                occasion,
                freeText,
                sortBy,
                sortDirection),
            pageable)
        .map(GarmentView::of);
  }

  @GetMapping("/{id}")
  public GarmentView detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return GarmentView.of(service.getOwned(userId, id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<GarmentView> patch(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) String ifMatch,
      @RequestBody GarmentPatchRequest request) {
    UUID userId = UUID.fromString(jwt.getSubject());
    long expectedVersion = parseIfMatch(ifMatch);
    Garment garment = service.updateOwned(userId, id, request, expectedVersion);
    return ResponseEntity.ok().eTag("\"" + garment.version() + "\"").body(GarmentView.of(garment));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    service.deleteOwned(userId, id);
  }

  private GarmentSource parseSource(String source) {
    if (source == null || source.isBlank()) {
      return GarmentSource.OWNED;
    }
    try {
      return GarmentSource.valueOf(source.trim().toUpperCase());
    } catch (IllegalArgumentException exception) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "invalid_garment_source", "Unsupported garment source.");
    }
  }

  private long parseIfMatch(String ifMatch) {
    if (ifMatch == null || ifMatch.isBlank()) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "missing_if_match",
          "PATCH requests must include an If-Match header with the current garment version.");
    }
    String normalized = ifMatch.trim();
    if (normalized.startsWith("W/\"")) {
      normalized = normalized.substring(3);
    }
    if (normalized.startsWith("\"") && normalized.endsWith("\"")) {
      normalized = normalized.substring(1, normalized.length() - 1);
    }
    try {
      return Long.parseLong(normalized);
    } catch (NumberFormatException exception) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "invalid_if_match",
          "If-Match must be a valid garment version ETag.");
    }
  }
}
