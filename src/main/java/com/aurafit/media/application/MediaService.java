package com.aurafit.media.application;

import com.aurafit.media.domain.MediaAsset;
import com.aurafit.media.domain.MediaKind;
import com.aurafit.media.domain.ModerationStatus;
import com.aurafit.shared.config.AuraFitProperties;
import com.aurafit.shared.error.ApiException;
import com.aurafit.trustsafety.application.ContentModerationPort;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {

  private final MediaAssetRepositoryPort mediaAssets;
  private final ObjectStoragePort objectStorage;
  private final ContentModerationPort contentModerationPort;
  private final AuraFitProperties properties;

  public MediaService(
      MediaAssetRepositoryPort mediaAssets,
      ObjectStoragePort objectStorage,
      ContentModerationPort contentModerationPort,
      AuraFitProperties properties) {
    this.mediaAssets = mediaAssets;
    this.objectStorage = objectStorage;
    this.contentModerationPort = contentModerationPort;
    this.properties = properties;
  }

  @Transactional
  public MediaAsset upload(UUID ownerUserId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "empty_upload", "A file is required.");
    }
    if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "invalid_file_name", "A filename is required.");
    }

    byte[] originalBytes;
    try {
      originalBytes = file.getBytes();
    } catch (IOException exception) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "invalid_upload", "Unable to read the uploaded file.");
    }

    AuraFitProperties.Upload upload = properties.media().upload();
    if (originalBytes.length > upload.maxFileSizeBytes()) {
      throw new ApiException(
          HttpStatus.PAYLOAD_TOO_LARGE,
          "file_too_large",
          "The uploaded file exceeds the configured size limit.");
    }

    byte[] normalized = normalizeImage(originalBytes, upload);
    String digest = sha256(normalized);
    String objectKey = "users/%s/%s.png".formatted(ownerUserId, UUID.randomUUID());

    if (!contentModerationPort.isAllowed("FACE_PHOTO", normalized, "image/png")) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "content_rejected",
          "The uploaded image was rejected by the moderation policy.");
    }

    objectStorage.store(objectKey, normalized, "image/png");

    Instant now = Instant.now();
    MediaAsset asset =
        new MediaAsset(
            UUID.randomUUID(),
            ownerUserId,
            MediaKind.FACE_PHOTO,
            objectKey,
            file.getOriginalFilename(),
            "image/png",
            normalized.length,
            readDimension(normalized).width(),
            readDimension(normalized).height(),
            digest,
            ModerationStatus.APPROVED,
            now,
            now.plus(Duration.ofDays(30)));
    return mediaAssets.save(asset);
  }

  @Transactional(readOnly = true)
  public MediaAsset findOwned(UUID ownerUserId, UUID mediaId) {
    MediaAsset asset =
        mediaAssets
            .findById(mediaId)
            .orElseThrow(
                () ->
                    new ApiException(HttpStatus.NOT_FOUND, "media_not_found", "Media not found."));
    if (!asset.ownerUserId().equals(ownerUserId)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "media_forbidden", "You do not own this media item.");
    }
    return asset;
  }

  @Transactional
  public void deleteOwned(UUID ownerUserId, UUID mediaId) {
    MediaAsset asset = findOwned(ownerUserId, mediaId);
    objectStorage.delete(asset.storageKey());
    mediaAssets.deleteById(asset.id());
  }

  private byte[] normalizeImage(byte[] source, AuraFitProperties.Upload upload) {
    if (source == null || source.length == 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_image", "An image file is required.");
    }

    String format = detectFormat(source);
    if (format == null) {
      throw new ApiException(
          HttpStatus.UNSUPPORTED_MEDIA_TYPE,
          "invalid_image_format",
          "Only JPEG, PNG, and WebP image files are accepted.");
    }

    BufferedImageDimension dimensions = readImageDimensions(source);
    int width = dimensions.width();
    int height = dimensions.height();
    if (width < upload.minWidth()
        || height < upload.minHeight()
        || width > upload.maxWidth()
        || height > upload.maxHeight()) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "invalid_dimensions",
          "Image dimensions are outside the allowed range.");
    }

    long pixels = (long) width * height;
    if (pixels > upload.maxPixels()) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "image_too_large",
          "The image exceeds the maximum pixel budget.");
    }

    BufferedImage image;
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(source)) {
      image = ImageIO.read(inputStream);
    } catch (IOException exception) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "invalid_image", "The image could not be decoded.");
    }

    if (image == null) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "invalid_image_payload",
          "The file content does not match a valid image.");
    }

    BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = rgbImage.createGraphics();
    try {
      graphics.drawImage(image, 0, 0, null);
    } finally {
      graphics.dispose();
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      if (!ImageIO.write(rgbImage, "png", outputStream)) {
        throw new ApiException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "encoding_failed",
            "The image could not be re-encoded safely.");
      }
    } catch (IOException exception) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "encoding_failed",
          "The image could not be re-encoded safely.");
    }
    return outputStream.toByteArray();
  }

  private String detectFormat(byte[] bytes) {
    if (bytes.length >= 8
        && bytes[0] == (byte) 0x89
        && bytes[1] == 'P'
        && bytes[2] == 'N'
        && bytes[3] == 'G'
        && bytes[4] == '\r'
        && bytes[5] == '\n'
        && bytes[6] == 0x1A
        && bytes[7] == '\n') {
      return "png";
    }
    if (bytes.length >= 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
      return "jpeg";
    }
    if (bytes.length >= 12
        && bytes[0] == 'R'
        && bytes[1] == 'I'
        && bytes[2] == 'F'
        && bytes[3] == 'F'
        && bytes[8] == 'W'
        && bytes[9] == 'E'
        && bytes[10] == 'B'
        && bytes[11] == 'P') {
      return "webp";
    }
    return null;
  }

  private String sha256(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(bytes);
      StringBuilder hex = new StringBuilder();
      for (byte value : hash) {
        hex.append(String.format(Locale.ROOT, "%02x", value));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private BufferedImageDimension readDimension(byte[] bytes) {
    return readImageDimensions(bytes);
  }

  private BufferedImageDimension readImageDimensions(byte[] bytes) {
    try (ImageInputStream inputStream =
        ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
      if (!readers.hasNext()) {
        throw new ApiException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "invalid_image_payload",
            "The file content does not match a valid image.");
      }

      ImageReader reader = readers.next();
      try {
        reader.setInput(inputStream);
        return new BufferedImageDimension(reader.getWidth(0), reader.getHeight(0));
      } catch (IOException exception) {
        throw new ApiException(
            HttpStatus.BAD_REQUEST, "invalid_image", "The image could not be decoded.");
      } finally {
        reader.dispose();
      }
    } catch (IOException exception) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "invalid_image", "The image could not be decoded.");
    }
  }

  private record BufferedImageDimension(int width, int height) {}
}
