package com.aurafit.identity.web;

import com.aurafit.identity.application.UserView;

public record RegistrationResponse(String message, UserView user) {}
