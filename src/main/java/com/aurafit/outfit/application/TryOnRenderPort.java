package com.aurafit.outfit.application;

import java.util.List;
import java.util.UUID;

public interface TryOnRenderPort {
  TryOnRenderResult render(UUID userId, UUID avatarId, List<UUID> garmentIds);

  record TryOnRenderResult(UUID mediaId, String provider, String model, String renderKey) {}
}
