package com.aurafit.identity.infrastructure.mail;

import com.aurafit.identity.application.EmailSenderPort;
import com.aurafit.shared.config.AuraFitProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class SmtpEmailSender implements EmailSenderPort {

  private final JavaMailSender mailSender;
  private final AuraFitProperties properties;
  private final String fromAddress;

  SmtpEmailSender(
      JavaMailSender mailSender,
      AuraFitProperties properties,
      @Value("${MAIL_FROM:no-reply@aurafit.local}") String fromAddress) {
    this.mailSender = mailSender;
    this.properties = properties;
    this.fromAddress = fromAddress;
  }

  @Override
  public void sendVerificationToken(String email, String rawToken) {
    String endpoint = properties.publicBaseUrl() + "/api/v1/auth/verify-email";
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(email);
    message.setSubject("Verify your AuraFit account");
    message.setText(
        "Submit this single-use token to the verification endpoint:\n\n"
            + endpoint
            + "\n\nRequest JSON:\n{\"token\":\""
            + rawToken
            + "\"}\n\nThe token expires in "
            + properties.identity().verification().tokenTtl().toHours()
            + " hours. If you did not create an account, ignore this email.");
    mailSender.send(message);
  }
}
