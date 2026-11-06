package com.aurafit.generation.web;

import com.aurafit.generation.application.GenerationJobService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs")
public class GenerationJobController {
  private final GenerationJobService service;

  public GenerationJobController(GenerationJobService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public GenerationJobView detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return GenerationJobView.of(service.findOwned(userId, id));
  }

  @GetMapping
  public Page<GenerationJobView> list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return service
        .listOwned(userId, Pageable.ofSize(size).withPage(page))
        .map(GenerationJobView::of);
  }

  @PostMapping("/{id}/cancel")
  public GenerationJobView cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return GenerationJobView.of(service.cancelOwned(userId, id));
  }
}
