package com.aurafit.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  ProblemDetail handleApiException(ApiException exception, HttpServletRequest request) {
    return problem(
        exception.status(), exception.code(), exception.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    String detail =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .distinct()
            .collect(Collectors.joining("; "));
    return problem(HttpStatus.BAD_REQUEST, "invalid_request", detail, request.getRequestURI());
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  ProblemDetail handleMethodValidation(
      HandlerMethodValidationException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "invalid_request",
        "Request parameters are invalid.",
        request.getRequestURI());
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  ProblemDetail handleMalformedRequest(Exception exception, HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "invalid_request",
        "The request body or parameters are invalid.",
        request.getRequestURI());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ProblemDetail handleDataConflict(
      DataIntegrityViolationException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.CONFLICT,
        "resource_conflict",
        "The request conflicts with an existing resource.",
        request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
    LOGGER.error("Unhandled API exception type={}", exception.getClass().getName());
    return problem(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "internal_error",
        "An unexpected error occurred.",
        request.getRequestURI());
  }

  private ProblemDetail problem(HttpStatus status, String code, String detail, String requestUri) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setType(URI.create("urn:aurafit:problem:" + code));
    problem.setTitle(status.getReasonPhrase());
    problem.setInstance(URI.create(requestUri));
    problem.setProperty("code", code);
    return problem;
  }
}
