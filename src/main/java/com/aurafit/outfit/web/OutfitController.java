package com.aurafit.outfit.web;

import com.aurafit.outfit.application.OutfitFilter;
import com.aurafit.outfit.application.OutfitService;
import com.aurafit.outfit.domain.Outfit;
import com.aurafit.shared.error.ApiException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.net.URI;
import java.util.List;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
public class OutfitController {
  private final OutfitService service;

  public OutfitController(OutfitService service) {
    this.service = service;
  }

  @PostMapping("/outfits")
  public ResponseEntity<OutfitView> create(
      @AuthenticationPrincipal Jwt jwt,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestBody OutfitCreateRequest request) {
    UUID userId = UUID.fromString(jwt.getSubject());
    Outfit outfit =
        service.create(
            userId,
            request == null ? null : request.name(),
            request == null || request.items() == null ? List.of() : request.items(),
            request != null && Boolean.TRUE.equals(request.favorite()));
    return ResponseEntity.accepted()
        .location(URI.create("/api/v1/outfits/" + outfit.id()))
        .body(OutfitView.of(outfit));
  }

  @GetMapping("/outfits")
  public Page<OutfitView> list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(required = false) String favorite,
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
            new OutfitFilter(favorite, season, occasion, freeText, sortBy, sortDirection),
            pageable)
        .map(OutfitView::of);
  }

  @GetMapping("/outfits/{id}")
  public OutfitView detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return OutfitView.of(service.getOwned(userId, id));
  }

  @PatchMapping("/outfits/{id}")
  public ResponseEntity<OutfitView> patch(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @RequestHeader(value = "If-Match", required = false) String ifMatch,
      @RequestBody OutfitPatchRequest request) {
    UUID userId = UUID.fromString(jwt.getSubject());
    long expectedVersion = parseIfMatch(ifMatch);
    Outfit outfit = service.updateOwned(userId, id, request, expectedVersion);
    return ResponseEntity.ok().eTag("\"" + outfit.version() + "\"").body(OutfitView.of(outfit));
  }

  @DeleteMapping("/outfits/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    service.deleteOwned(userId, id);
  }

  @PutMapping("/outfits/{id}/favorite")
  public OutfitView setFavorite(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @RequestParam(defaultValue = "true") boolean favorite) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return OutfitView.of(service.toggleFavorite(userId, id, favorite));
  }

  @PostMapping("/outfits/{id}/render")
  public ResponseEntity<Void> render(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    UUID jobId = service.renderOwned(userId, id);
    return ResponseEntity.accepted().location(URI.create("/api/v1/jobs/" + jobId)).build();
  }

  @PostMapping("/tryon")
  public ResponseEntity<Void> tryOn(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam UUID avatarId,
      @RequestBody OutfitCreateRequest request) {
    UUID userId = UUID.fromString(jwt.getSubject());
    UUID jobId =
        service.renderAdHoc(
            userId,
            request == null || request.items() == null ? List.of() : request.items(),
            avatarId);
    return ResponseEntity.accepted().location(URI.create("/api/v1/jobs/" + jobId)).build();
  }

  private long parseIfMatch(String ifMatch) {
    if (ifMatch == null || ifMatch.isBlank()) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "missing_if_match",
          "PATCH requests must include an If-Match header with the current outfit version.");
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
          "If-Match must be a valid outfit version ETag.");
    }
  }
}
