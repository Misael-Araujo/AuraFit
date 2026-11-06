package com.aurafit.media.application;

import java.time.Duration;

public interface ObjectStoragePort {
  void store(String key, byte[] content, String contentType);

  byte[] fetch(String key);

  void delete(String key);

  String generatePresignedGetUrl(String key, Duration ttl);
}
