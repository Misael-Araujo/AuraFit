package com.aurafit.media.web;

import com.aurafit.media.application.MediaService;
import com.aurafit.media.application.ObjectStoragePort;
import com.aurafit.media.domain.MediaAsset;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
@SecurityRequirement(name = "bearerAuth")
public class MediaController {

  private final MediaService mediaService;
  private final ObjectStoragePort objectStorage;

  public MediaController(MediaService mediaService, ObjectStoragePort objectStorage) {
    this.mediaService = mediaService;
    this.objectStorage = objectStorage;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MediaAssetView upload(
      @AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file) {
    UUID userId = UUID.fromString(jwt.getSubject());
    MediaAsset media = mediaService.upload(userId, file);
    return MediaAssetView.of(
        media, objectStorage.generatePresignedGetUrl(media.storageKey(), Duration.ofMinutes(5)));
  }

  @GetMapping("/{id}")
  public MediaAssetView detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    MediaAsset media = mediaService.findOwned(userId, id);
    return MediaAssetView.of(
        media, objectStorage.generatePresignedGetUrl(media.storageKey(), Duration.ofMinutes(5)));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    mediaService.deleteOwned(userId, id);
  }
}
