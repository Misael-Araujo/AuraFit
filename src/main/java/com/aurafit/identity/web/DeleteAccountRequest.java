package com.aurafit.identity.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
    @NotBlank String currentPassword, @Email @NotBlank String confirmationEmail) {}
