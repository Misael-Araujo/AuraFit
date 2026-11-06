package com.aurafit.media.infrastructure.persistence;

import com.aurafit.media.domain.MediaAsset;
import com.aurafit.media.domain.MediaKind;
import com.aurafit.media.domain.ModerationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_asset")
public class MediaAssetEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "kind", nullable = false)
  private MediaKind kind;

  @Column(name = "storage_key", nullable = false, unique = true)
  private String storageKey;

  @Column(name = "original_filename", nullable = false)
  private String originalFileName;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "width", nullable = false)
  private int width;

  @Column(name = "height", nullable = false)
  private int height;

  @Column(name = "sha256", nullable = false, unique = true)
  private String sha256;

  @Enumerated(EnumType.STRING)
  @Column(name = "moderation_status", nullable = false)
  private ModerationStatus moderationStatus;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "retention_until", nullable = false)
  private Instant retentionUntil;

  protected MediaAssetEntity() {}

  public MediaAssetEntity(
      UUID id,
      UUID userId,
      MediaKind kind,
      String storageKey,
      String originalFileName,
      String contentType,
      long sizeBytes,
      int width,
      int height,
      String sha256,
      ModerationStatus moderationStatus,
      Instant createdAt,
      Instant retentionUntil) {
    this.id = id;
    this.userId = userId;
    this.kind = kind;
    this.storageKey = storageKey;
    this.originalFileName = originalFileName;
    this.contentType = contentType;
    this.sizeBytes = sizeBytes;
    this.width = width;
    this.height = height;
    this.sha256 = sha256;
    this.moderationStatus = moderationStatus;
    this.createdAt = createdAt;
    this.retentionUntil = retentionUntil;
  }

  public MediaAsset toAggregate() {
    return new MediaAsset(
        id,
        userId,
        kind,
        storageKey,
        originalFileName,
        contentType,
        sizeBytes,
        width,
        height,
        sha256,
        moderationStatus,
        createdAt,
        retentionUntil);
  }

  public static MediaAssetEntity fromAggregate(MediaAsset asset) {
    return new MediaAssetEntity(
        asset.id(),
        asset.ownerUserId(),
        asset.kind(),
        asset.storageKey(),
        asset.originalFileName(),
        asset.contentType(),
        asset.sizeBytes(),
        asset.width(),
        asset.height(),
        asset.sha256(),
        asset.moderationStatus(),
        asset.createdAt(),
        asset.retentionUntil());
  }
}
