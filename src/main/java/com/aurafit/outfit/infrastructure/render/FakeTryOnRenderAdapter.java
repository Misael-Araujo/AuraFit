package com.aurafit.outfit.infrastructure.render;

import com.aurafit.outfit.application.TryOnRenderPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakeTryOnRenderAdapter implements TryOnRenderPort {

  @Override
  public TryOnRenderResult render(UUID userId, UUID avatarId, List<UUID> garmentIds) {
    String renderKey =
        avatarId
            + ":"
            + String.join("|", garmentIds.stream().sorted().map(UUID::toString).toList());
    return new TryOnRenderResult(
        UUID.randomUUID(), "fake-renderer", "placeholder-tryon-model", renderKey);
  }
}
