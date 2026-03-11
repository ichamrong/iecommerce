package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.exception.AccountLockedException;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.application.exception.RateLimitExceededException;
import com.chamrong.iecommerce.auth.domain.exception.AuthException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception-to-HTTP-response mapping for the auth module.
 *
 * <p>Covers OWASP A07 (auth failures), A09 (logging), and A03 (input validation errors).
 *
 * <p>Uses {@link Ordered#HIGHEST_PRECEDENCE} so auth exceptions (e.g. BadCredentialsException) are
 * mapped to 401 before the global catch-all returns 500.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(AuthException.class)
  public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
    log.warn("Auth exception: {}", ex.getMessage());
    return buildBody(ex.getErrorCode().getStatus(), ex.getErrorCode().getCode(), ex.getMessage());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
    log.warn("Bad credentials: {}", ex.getMessage());
    return buildBody(HttpStatus.UNAUTHORIZED, "AUTH-002", "Invalid username or password");
  }

  /** HTTP 429 — IP rate limit exceeded. */
  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<Map<String, Object>> handleRateLimit(RateLimitExceededException ex) {
    log.warn("Rate limit exceeded: {}", ex.getMessage());
    return buildBody(HttpStatus.TOO_MANY_REQUESTS, "AUTH-003", ex.getMessage());
  }

  /** HTTP 423 — Account is locked / disabled in IDP. */
  @ExceptionHandler(AccountLockedException.class)
  public ResponseEntity<Map<String, Object>> handleAccountLocked(AccountLockedException ex) {
    log.warn("Account locked: {}", ex.getMessage());
    return buildBody(HttpStatus.LOCKED, "AUTH-004", ex.getMessage());
  }

  /** HTTP 409 — Duplicate username or email. */
  @ExceptionHandler(DuplicateUserException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateUserException ex) {
    log.warn("Duplicate user: {}", ex.getMessage());
    return buildBody(HttpStatus.CONFLICT, "AUTH-005", ex.getMessage());
  }

  /**
   * HTTP 400 — Bean Validation failure (@Valid on request body).
   *
   * <p>Returns a structured list of field-level errors (OWASP A03 — Injection).
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    var errors = new HashMap<String, String>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Validation Failed");
    body.put("code", "AUTH-006");
    body.put("fields", errors);
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  private ResponseEntity<Map<String, Object>> buildBody(
      HttpStatus status, String code, String message) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("code", code);
    body.put("message", message);
    return new ResponseEntity<>(body, status);
  }
}
