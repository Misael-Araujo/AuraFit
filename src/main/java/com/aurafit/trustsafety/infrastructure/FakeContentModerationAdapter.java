package com.aurafit.trustsafety.infrastructure;

import com.aurafit.trustsafety.application.ContentModerationPort;
import org.springframework.stereotype.Component;

@Component
public class FakeContentModerationAdapter implements ContentModerationPort {

  private final TrustSafetyProperties properties;

  public FakeContentModerationAdapter(TrustSafetyProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean isAllowed(String contentKind, byte[] content, String contentType) {
    if (!properties.moderation().enabled()) {
      return true;
    }
    return properties.moderation().defaultApproval();
  }
}
