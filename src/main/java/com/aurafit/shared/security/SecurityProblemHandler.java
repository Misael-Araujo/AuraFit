package com.aurafit.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public SecurityProblemHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    response.setHeader("WWW-Authenticate", "Bearer");
    writeProblem(
        request,
        response,
        HttpStatus.UNAUTHORIZED,
        "authentication_required",
        "Authentication is required.");
  }

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
      throws IOException {
    writeProblem(
        request,
        response,
        HttpStatus.FORBIDDEN,
        "access_denied",
        "You are not allowed to perform this operation.");
  }

  private void writeProblem(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      String code,
      String detail)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    objectMapper.writeValue(
        response.getOutputStream(),
        Map.of(
            "type", URI.create("urn:aurafit:problem:" + code).toString(),
            "title", status.getReasonPhrase(),
            "status", status.value(),
            "detail", detail,
            "instance", request.getRequestURI(),
            "code", code));
  }
}
