package com.aurafit.trustsafety.application;

public interface ContentModerationPort {
  boolean isAllowed(String contentKind, byte[] content, String contentType);
}
