package com.aurafit.avatar.infrastructure.persistence;

import com.aurafit.avatar.domain.Avatar;
import com.aurafit.avatar.domain.AvatarStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "avatar")
public class AvatarEntity {
  @Id private UUID id;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private AvatarStatus status;

  @Column(name = "source_media_id", nullable = false)
  private UUID sourceMediaId;

  @Column(name = "result_media_id")
  private UUID resultMediaId;

  @Column(name = "version", nullable = false)
  private String version;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "body_measurements", length = 1024)
  private String bodyMeasurements;

  public AvatarEntity() {}

  public static AvatarEntity fromAggregate(Avatar avatar) {
    AvatarEntity entity = new AvatarEntity();
    entity.id = avatar.id();
    entity.ownerUserId = avatar.ownerUserId();
    entity.status = avatar.status();
    entity.sourceMediaId = avatar.sourceMediaId();
    entity.resultMediaId = avatar.resultMediaId();
    entity.version = avatar.version();
    entity.createdAt = avatar.createdAt();
    entity.updatedAt = avatar.updatedAt();
    entity.bodyMeasurements = avatar.bodyMeasurements();
    return entity;
  }

  public Avatar toAggregate() {
    return new Avatar(
        id,
        ownerUserId,
        status,
        sourceMediaId,
        resultMediaId,
        version,
        createdAt,
        updatedAt,
        bodyMeasurements);
  }
}
