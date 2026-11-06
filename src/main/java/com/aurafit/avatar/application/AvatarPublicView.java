package com.aurafit.avatar.application;

import java.util.UUID;

public record AvatarPublicView(UUID id, UUID ownerUserId, String status, String version) {}
