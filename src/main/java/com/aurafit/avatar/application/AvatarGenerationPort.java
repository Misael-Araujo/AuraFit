package com.aurafit.avatar.application;

import java.util.UUID;

public interface AvatarGenerationPort {
  byte[] generate(UUID userId, byte[] sourceImage, String contentType);
}
