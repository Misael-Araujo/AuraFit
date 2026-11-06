package com.aurafit.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aurafit")
public record AuraFitProperties(
    @NotBlank String publicBaseUrl,
    @Valid Cors cors,
    @Valid Security security,
    @Valid Identity identity,
    @Valid Media media,
    @Valid RateLimit rateLimit) {

  public AuraFitProperties {
    if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
      publicBaseUrl = "http://localhost:8080";
    }
    if (cors == null) {
      cors = new Cors(List.of("http://localhost:3000"));
    }
    if (security == null) {
      security =
          new Security(
              new Jwt(
                  "aurafit",
                  "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                  Duration.ofMinutes(15)));
    }
    if (identity == null) {
      identity = new Identity(new Verification(Duration.ofHours(24)));
    }
    if (media == null) {
      media =
          new Media(
              new Storage(
                  "http://localhost:9000",
                  "aurafit-private",
                  "us-east-1",
                  "aurafit-local",
                  "aurafit-minio-local-only",
                  true),
              new Upload(10 * 1024 * 1024, 25_000_000L, 32, 32, 12_000, 12_000));
    }
    if (rateLimit == null) {
      rateLimit = new RateLimit(20);
    }
  }

  public record Cors(@NotEmpty List<@NotBlank String> allowedOrigins) {}

  public record Security(@Valid Jwt jwt) {}

  public record Jwt(
      @NotBlank String issuer,
      @NotBlank @Size(min = 40) String secret,
      @NotNull Duration accessTokenTtl) {
    public Jwt {
      if (accessTokenTtl == null || accessTokenTtl.isZero() || accessTokenTtl.isNegative()) {
        throw new IllegalArgumentException("JWT access-token TTL must be positive.");
      }
    }
  }

  public record Identity(@Valid Verification verification) {}

  public record Verification(@NotNull Duration tokenTtl) {
    public Verification {
      if (tokenTtl == null || tokenTtl.isZero() || tokenTtl.isNegative()) {
        throw new IllegalArgumentException("Email verification token TTL must be positive.");
      }
    }
  }

  public record Media(@Valid Storage storage, @Valid Upload upload) {}

  public record Storage(
      @NotBlank String endpoint,
      @NotBlank String bucket,
      @NotBlank String region,
      @NotBlank String accessKey,
      @NotBlank String secretKey,
      boolean forcePathStyle) {}

  public record Upload(
      @Positive long maxFileSizeBytes,
      @Positive long maxPixels,
      @Positive int minWidth,
      @Positive int minHeight,
      @Positive int maxWidth,
      @Positive int maxHeight) {}

  public record RateLimit(@Positive int authRequestsPerMinute) {}
}
