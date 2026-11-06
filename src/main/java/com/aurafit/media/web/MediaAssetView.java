package com.aurafit.media.web;

import com.aurafit.media.domain.MediaAsset;
import com.aurafit.media.domain.MediaKind;
import java.time.Instant;
import java.util.UUID;

public record MediaAssetView(
    UUID id,
    UUID ownerUserId,
    MediaKind kind,
    String originalFileName,
    String contentType,
    long sizeBytes,
    int width,
    int height,
    String sha256,
    String moderationStatus,
    String presignedUrl,
    Instant createdAt) {

  public static MediaAssetView of(MediaAsset asset, String presignedUrl) {
    return new MediaAssetView(
        asset.id(),
        asset.ownerUserId(),
        asset.kind(),
        asset.originalFileName(),
        asset.contentType(),
        asset.sizeBytes(),
        asset.width(),
        asset.height(),
        asset.sha256(),
        asset.moderationStatus().name(),
        presignedUrl,
        asset.createdAt());
  }
}
