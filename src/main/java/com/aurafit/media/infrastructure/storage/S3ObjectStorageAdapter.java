package com.aurafit.media.infrastructure.storage;

import com.aurafit.media.application.ObjectStoragePort;
import com.aurafit.shared.config.AuraFitProperties;
import java.io.IOException;
import java.time.Duration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
public class S3ObjectStorageAdapter implements ObjectStoragePort {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucket;

  public S3ObjectStorageAdapter(
      S3Client s3Client, S3Presigner s3Presigner, AuraFitProperties properties) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.bucket = properties.media().storage().bucket();
  }

  @Override
  public void store(String key, byte[] content, String contentType) {
    s3Client.putObject(
        PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
        RequestBody.fromBytes(content));
  }

  @Override
  public byte[] fetch(String key) {
    try (ResponseInputStream<GetObjectResponse> objectStream =
        s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())) {
      return objectStream.readAllBytes();
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to read object from storage.", exception);
    }
  }

  @Override
  public void delete(String key) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }

  @Override
  public String generatePresignedGetUrl(String key, Duration ttl) {
    GetObjectPresignRequest request =
        GetObjectPresignRequest.builder()
            .signatureDuration(ttl)
            .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
            .build();
    return s3Presigner.presignGetObject(request).url().toString();
  }
}
