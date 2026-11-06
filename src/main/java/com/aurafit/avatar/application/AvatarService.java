package com.aurafit.avatar.application;

import com.aurafit.avatar.domain.Avatar;
import com.aurafit.avatar.domain.AvatarStatus;
import com.aurafit.consent.application.ConsentService;
import com.aurafit.consent.domain.ConsentPurpose;
import com.aurafit.generation.application.JobQueuePort;
import com.aurafit.media.application.MediaAssetRepositoryPort;
import com.aurafit.media.domain.MediaAsset;
import com.aurafit.shared.error.ApiException;
import com.aurafit.trustsafety.application.IdentityVerificationPort;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvatarService {
  private final AvatarRepositoryPort avatars;
  private final MediaAssetRepositoryPort mediaAssets;
  private final ConsentService consentService;
  private final IdentityVerificationPort identityVerificationPort;
  private final JobQueuePort jobQueuePort;

  public AvatarService(
      AvatarRepositoryPort avatars,
      MediaAssetRepositoryPort mediaAssets,
      ConsentService consentService,
      IdentityVerificationPort identityVerificationPort,
      JobQueuePort jobQueuePort) {
    this.avatars = avatars;
    this.mediaAssets = mediaAssets;
    this.consentService = consentService;
    this.identityVerificationPort = identityVerificationPort;
    this.jobQueuePort = jobQueuePort;
  }

  @Transactional
  public AvatarCreationResult create(UUID userId, UUID sourceMediaId) {
    MediaAsset source =
        mediaAssets
            .findById(sourceMediaId)
            .orElseThrow(
                () ->
                    new ApiException(HttpStatus.NOT_FOUND, "media_not_found", "Media not found."));
    if (!source.ownerUserId().equals(userId)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "media_forbidden", "You do not own this media item.");
    }
    boolean hasBiometricConsent =
        consentService.hasCurrentGrantedConsent(userId, ConsentPurpose.BIOMETRIC_PROCESSING)
            || consentService.hasCurrentGrantedConsent(userId, ConsentPurpose.AVATAR_CREATION);
    if (!hasBiometricConsent) {
      throw new ApiException(
          HttpStatus.FORBIDDEN,
          "consent_required",
          "Avatar creation requires current consent for biometric processing and a fresh policy version.");
    }
    if (!identityVerificationPort.isOwnerVerified(userId)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN,
          "identity_verification_required",
          "Identity verification is required before creating an avatar.");
    }
    Avatar avatar =
        new Avatar(
            UUID.randomUUID(),
            userId,
            AvatarStatus.PENDING,
            sourceMediaId,
            null,
            "v1",
            Instant.now(),
            Instant.now(),
            null);
    avatars.save(avatar);
    UUID generatedJobId =
        jobQueuePort.submit(
            userId, "avatar_generation", sourceMediaId.toString(), "avatar:" + sourceMediaId);
    return new AvatarCreationResult(avatar, generatedJobId);
  }

  @Transactional(readOnly = true)
  public Avatar getCurrent(UUID userId) {
    return avatars
        .findCurrentByUserId(userId)
        .orElseThrow(
            () ->
                new ApiException(
                    HttpStatus.NOT_FOUND, "avatar_not_found", "You do not have an avatar yet."));
  }

  @Transactional
  public void delete(UUID userId, UUID avatarId) {
    Avatar avatar =
        avatars
            .findById(avatarId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND, "avatar_not_found", "Avatar not found."));
    if (!avatar.ownerUserId().equals(userId)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "avatar_forbidden", "You do not own this avatar.");
    }
    avatars.deleteById(avatar.id());
  }
}
