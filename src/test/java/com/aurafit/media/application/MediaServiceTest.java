package com.aurafit.media.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aurafit.media.domain.MediaAsset;
import com.aurafit.media.domain.MediaKind;
import com.aurafit.shared.config.AuraFitProperties;
import com.aurafit.shared.error.ApiException;
import com.aurafit.trustsafety.application.ContentModerationPort;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

  @Mock private MediaAssetRepositoryPort mediaAssets;
  @Mock private ObjectStoragePort objectStorage;
  @Mock private ContentModerationPort contentModerationPort;

  private MediaService mediaService;
  private final AuraFitProperties properties =
      new AuraFitProperties(
          "http://localhost:8080",
          new AuraFitProperties.Cors(List.of("http://localhost:3000")),
          new AuraFitProperties.Security(
              new AuraFitProperties.Jwt(
                  "aurafit",
                  "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                  Duration.ofMinutes(15))),
          new AuraFitProperties.Identity(new AuraFitProperties.Verification(Duration.ofHours(24))),
          new AuraFitProperties.Media(
              new AuraFitProperties.Storage(
                  "http://localhost:9000",
                  "aurafit-private",
                  "us-east-1",
                  "aurafit-local",
                  "aurafit-minio-local-only",
                  true),
              new AuraFitProperties.Upload(10 * 1024 * 1024, 25_000_000L, 32, 32, 12_000, 12_000)),
          new AuraFitProperties.RateLimit(20));

  @BeforeEach
  void setUp() {
    mediaService = new MediaService(mediaAssets, objectStorage, contentModerationPort, properties);
    lenient()
        .when(mediaAssets.save(any(MediaAsset.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void acceptsValidImageAndStoresPrivateObject() throws Exception {
    UUID userId = UUID.randomUUID();
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.png", "image/png", createPng(200, 200));
    when(contentModerationPort.isAllowed(anyString(), any(byte[].class), anyString()))
        .thenReturn(true);

    MediaAsset asset = mediaService.upload(userId, file);

    assertThat(asset.kind()).isEqualTo(MediaKind.FACE_PHOTO);
    assertThat(asset.storageKey()).startsWith("users/" + userId + "/");
    assertThat(asset.contentType()).isEqualTo("image/png");
    verify(objectStorage).store(anyString(), any(byte[].class), eq("image/png"));
  }

  @Test
  void rejectsInvalidMagicBytes() {
    MockMultipartFile file =
        new MockMultipartFile("file", "fake.png", "image/png", "not-a-real-image".getBytes());

    assertThatThrownBy(() -> mediaService.upload(UUID.randomUUID(), file))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Only JPEG, PNG, and WebP");
  }

  @Test
  void rejectsOversizedImageDimensions() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "big.png", "image/png", createPng(5_001, 5_001));

    assertThatThrownBy(() -> mediaService.upload(UUID.randomUUID(), file))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("maximum pixel budget");
  }

  private byte[] createPng(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }
}
