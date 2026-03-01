package com.chamrong.iecommerce.catalog.api;

import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler scoped to all catalog API controllers.
 *
 * <p>Maps domain exceptions to HTTP status codes and {@link CatalogErrorResponse}.
 */
@RestControllerAdvice(basePackages = "com.chamrong.iecommerce.catalog.api")
@Slf4j
public class CatalogExceptionHandler {

  /** Optimistic locking conflict — concurrent write detected. */
  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<CatalogErrorResponse> handleOle(OptimisticLockingFailureException ex) {
    log.warn("[Catalog] Optimistic locking conflict: {}", ex.getMessage());
    return conflict("CATALOG_CONFLICT", "The resource was modified concurrently. Please retry.");
  }

  /** Invalid lifecycle transition, slug conflict, duplicate SKU, etc. */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<CatalogErrorResponse> handleIllegalState(IllegalStateException ex) {
    log.warn("[Catalog] Illegal state: {}", ex.getMessage());
    return conflict("CATALOG_INVALID_STATE", ex.getMessage());
  }

  /** Invalid cursor (malformed, version, or filter hash mismatch). */
  @ExceptionHandler(InvalidCursorException.class)
  public ResponseEntity<CatalogErrorResponse> handleInvalidCursor(InvalidCursorException ex) {
    log.debug("[Catalog] Invalid cursor: {} - {}", ex.getErrorCode(), ex.getMessage());
    return badRequest(
        ex.getErrorCode() != null ? ex.getErrorCode() : "INVALID_CURSOR", ex.getMessage());
  }

  /** Slug conflicts, invalid input, malformed cursor. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<CatalogErrorResponse> handleIllegalArg(IllegalArgumentException ex) {
    log.warn("[Catalog] Bad argument: {}", ex.getMessage());
    return badRequest("CATALOG_BAD_REQUEST", ex.getMessage());
  }

  /** Resource not found. */
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<CatalogErrorResponse> handleNotFound(EntityNotFoundException ex) {
    log.debug("[Catalog] Not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new CatalogErrorResponse(ex.getMessage(), "CATALOG_NOT_FOUND"));
  }

  /** Bean Validation failures on @RequestBody / @Valid. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<CatalogErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new java.util.LinkedHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
    String message = "Validation failed: " + errors;
    log.debug("[Catalog] Validation error: {}", message);
    return badRequest("CATALOG_VALIDATION_ERROR", message);
  }

  /** Jakarta Validation failures on @Validated services. */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<CatalogErrorResponse> handleConstraint(ConstraintViolationException ex) {
    log.debug("[Catalog] Constraint violation: {}", ex.getMessage());
    return badRequest("CATALOG_VALIDATION_ERROR", ex.getMessage());
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private ResponseEntity<CatalogErrorResponse> conflict(String code, String message) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new CatalogErrorResponse(message, code));
  }

  private ResponseEntity<CatalogErrorResponse> badRequest(String code, String message) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new CatalogErrorResponse(message, code));
  }
}
