package com.aurafit.consent.web;

import com.aurafit.consent.application.ConsentService;
import com.aurafit.consent.domain.ConsentPurpose;
import com.aurafit.consent.domain.ConsentRecord;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consents")
@SecurityRequirement(name = "bearerAuth")
public class ConsentController {

  private final ConsentService consentService;

  public ConsentController(ConsentService consentService) {
    this.consentService = consentService;
  }

  @GetMapping
  public List<ConsentRecord> current(@AuthenticationPrincipal Jwt jwt) {
    return consentService.current(UUID.fromString(jwt.getSubject()));
  }

  @PutMapping("/{purpose}")
  public ConsentRecord update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable ConsentPurpose purpose,
      @Valid @RequestBody ConsentRequest request) {
    return consentService.record(
        UUID.fromString(jwt.getSubject()), purpose, request.granted(), request.policyVersion());
  }

  public record ConsentRequest(boolean granted, @NotBlank @Size(max = 40) String policyVersion) {}
}
