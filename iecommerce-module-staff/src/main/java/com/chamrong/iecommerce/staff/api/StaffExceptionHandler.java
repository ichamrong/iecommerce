package com.chamrong.iecommerce.staff.api;

import com.chamrong.iecommerce.staff.domain.StaffAccessDeniedException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for the Staff API.
 *
 * <p>Converts domain exceptions to the appropriate HTTP status codes and structured error bodies,
 * preventing framework stack-traces from leaking to clients.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = StaffController.class)
public class StaffExceptionHandler {

  /** Concurrent update conflict (optimistic locking lost the race). */
  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<StaffErrorResponse> handleOptimisticLock(
      OptimisticLockingFailureException ex) {
    log.warn("STAFF_OPTIMISTIC_LOCK_CONFLICT: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            StaffErrorResponse.of(
                "The record was modified by another request. Please retry.", "STAFF_CONFLICT"));
  }

  /** Domain invariant violated (e.g. updating a terminated staff member). */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<StaffErrorResponse> handleIllegalState(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(StaffErrorResponse.of(ex.getMessage(), "STAFF_INVALID_STATE"));
  }

  /** Duplicate staff creation. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<StaffErrorResponse> handleIllegalArg(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(StaffErrorResponse.of(ex.getMessage(), "STAFF_ALREADY_EXISTS"));
  }

  /** Staff ID not found. */
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<StaffErrorResponse> handleNotFound(EntityNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(StaffErrorResponse.of(ex.getMessage(), "STAFF_NOT_FOUND"));
  }

  /**
   * Access denied for the requested staff member (e.g. cross-tenant access). We deliberately return
   * 404 to avoid leaking the existence of resources across tenants.
   */
  @ExceptionHandler(StaffAccessDeniedException.class)
  public ResponseEntity<StaffErrorResponse> handleAccessDenied(StaffAccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(StaffErrorResponse.of(ex.getMessage(), "STAFF_NOT_FOUND"));
  }

  /** Bean Validation failures on @RequestBody. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<StaffErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");
    return ResponseEntity.badRequest().body(StaffErrorResponse.of(message, "STAFF_INVALID_INPUT"));
  }

  /** Bean Validation failures on @Validated method params. */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<StaffErrorResponse> handleConstraint(ConstraintViolationException ex) {
    return ResponseEntity.badRequest()
        .body(StaffErrorResponse.of(ex.getMessage(), "STAFF_INVALID_INPUT"));
  }
}
