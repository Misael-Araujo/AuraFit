package com.aurafit.avatar.web;

import com.aurafit.avatar.application.AvatarCreationResult;
import com.aurafit.avatar.application.AvatarService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/avatars")
@SecurityRequirement(name = "bearerAuth")
public class AvatarController {
  private final AvatarService avatarService;

  public AvatarController(AvatarService avatarService) {
    this.avatarService = avatarService;
  }

  @PostMapping
  public ResponseEntity<AvatarView> create(
      @AuthenticationPrincipal Jwt jwt, @RequestBody CreateAvatarRequest request) {
    UUID userId = UUID.fromString(jwt.getSubject());
    AvatarCreationResult result = avatarService.create(userId, request.sourceMediaId());
    return ResponseEntity.accepted()
        .location(URI.create("/api/v1/jobs/" + result.jobId()))
        .body(AvatarView.of(result.avatar()));
  }

  @GetMapping("/me")
  public AvatarView getCurrent(@AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return AvatarView.of(avatarService.getCurrent(userId));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    UUID userId = UUID.fromString(jwt.getSubject());
    avatarService.delete(userId, id);
  }
}
