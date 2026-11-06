package com.aurafit.identity.application;

public interface EmailSenderPort {

  void sendVerificationToken(String email, String rawToken);
}
