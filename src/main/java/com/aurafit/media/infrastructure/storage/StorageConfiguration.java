package com.aurafit.media.infrastructure.storage;

import com.aurafit.shared.config.AuraFitProperties;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class StorageConfiguration {

  @Bean
  S3Client s3Client(AuraFitProperties properties) {
    AuraFitProperties.Storage storage = properties.media().storage();
    return S3Client.builder()
        .endpointOverride(URI.create(storage.endpoint()))
        .region(Region.of(storage.region()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(storage.accessKey(), storage.secretKey())))
        .serviceConfiguration(
            S3Configuration.builder().pathStyleAccessEnabled(storage.forcePathStyle()).build())
        .build();
  }

  @Bean
  S3Presigner s3Presigner(AuraFitProperties properties) {
    AuraFitProperties.Storage storage = properties.media().storage();
    return S3Presigner.builder()
        .endpointOverride(URI.create(storage.endpoint()))
        .region(Region.of(storage.region()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(storage.accessKey(), storage.secretKey())))
        .serviceConfiguration(
            S3Configuration.builder().pathStyleAccessEnabled(storage.forcePathStyle()).build())
        .build();
  }
}
