package com.chamrong.iecommerce.common;

import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.common.security.CapabilityDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.view.RedirectView;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(InvalidCursorException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidCursor(
      InvalidCursorException ex, HttpServletRequest request) {
    log.warn("Invalid cursor: {} [{}]", ex.getMessage(), ex.getErrorCode());
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    body.put("errorCode", ex.getErrorCode());
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(CapabilityDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleCapabilityDenied(
      CapabilityDeniedException ex, HttpServletRequest request) {
    log.warn("Capability denied: {} [{}]", ex.getMessage(), ex.getErrorCode());
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.FORBIDDEN.value());
    body.put("error", "Forbidden");
    body.put("message", ex.getMessage());
    body.put("errorCode", ex.getErrorCode());
    return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
  }

  /** Handles invalid arguments (e.g. entity not found). Returns 400 so clients do not see 500. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    String correlationId = UUID.randomUUID().toString();
    log.warn("Invalid argument [CorrelationID: {}]: {}", correlationId, ex.getMessage());
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    body.put("correlationId", correlationId);
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles invalid state (e.g. business rule violation). Returns 409 so clients do not see 500.
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalState(
      IllegalStateException ex, HttpServletRequest request) {
    String correlationId = UUID.randomUUID().toString();
    log.warn("Invalid state [CorrelationID: {}]: {}", correlationId, ex.getMessage());
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.CONFLICT.value());
    body.put("error", "Conflict");
    body.put("message", ex.getMessage());
    body.put("correlationId", correlationId);
    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public Object handleAllExceptions(Exception ex, HttpServletRequest request) {
    String correlationId = UUID.randomUUID().toString();

    log.error("Unhandled Exception [CorrelationID: {}]: {}", correlationId, ex.getMessage(), ex);

    String acceptHeader = request.getHeader("Accept");
    boolean isBrowserRequest =
        acceptHeader != null && acceptHeader.contains(MediaType.TEXT_HTML_VALUE);

    if (isBrowserRequest) {
      // Redirect browser to the beautiful Thymeleaf 500 page
      return new RedirectView("/error/500");
    }

    // Return structured JSON for API clients (curl, Swagger, mobile apps, etc.)
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Internal Server Error");
    body.put(
        "message",
        "An unexpected error occurred. Please contact support with Correlation ID: "
            + correlationId);
    body.put("correlationId", correlationId);

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
