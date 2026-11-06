package com.aurafit.avatar.application;

import com.aurafit.avatar.domain.Avatar;
import java.util.UUID;

public record AvatarCreationResult(Avatar avatar, UUID jobId) {}
