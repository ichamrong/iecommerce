package com.chamrong.iecommerce.asset.api;

import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.AssetException;
import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * REST controller advice for the Asset module. Provides structured, secure error responses for
 * domain exceptions.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = AssetController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AssetExceptionHandler {

  @ExceptionHandler(AssetException.class)
  public ResponseEntity<ErrorResponse> handleAssetException(
      AssetException ex, HttpServletRequest request) {
    log.error(
        "Asset Domain Exception: [Code: {}] {}", ex.getErrorCode().getCode(), ex.getMessage());

    HttpStatus status = mapToStatus(ex.getErrorCode());

    ErrorResponse response =
        ErrorResponse.builder()
            .timestamp(Instant.now())
            .code(ex.getErrorCode().getCode())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();

    return new ResponseEntity<>(response, status);
  }

  @ExceptionHandler(SecurityValidationException.class)
  public ResponseEntity<ErrorResponse> handleSecurityException(
      SecurityValidationException ex, HttpServletRequest request) {
    log.warn("SECURITY ALERT: {} at path {}", ex.getMessage(), request.getRequestURI());

    ErrorResponse response =
        ErrorResponse.builder()
            .timestamp(Instant.now())
            .code(ex.getErrorCode().getCode())
            .message("Security validation failed. Operation aborted.")
            .path(request.getRequestURI())
            .build();

    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(StorageException.class)
  public ResponseEntity<ErrorResponse> handleStorageException(
      StorageException ex, HttpServletRequest request) {
    log.error("Storage Backend Failure: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .timestamp(Instant.now())
            .code(ex.getErrorCode().getCode())
            .message("The storage service is temporarily unavailable.")
            .path(request.getRequestURI())
            .build();

    return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
  }

  private HttpStatus mapToStatus(AssetErrorCode code) {
    return switch (code) {
      case ASSET_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case ASSET_ALREADY_EXISTS -> HttpStatus.CONFLICT;
      case UNAUTHORIZED_ACCESS -> HttpStatus.UNAUTHORIZED;
      case INSECURE_FILE, PATH_TRAVERSAL_ATTEMPT -> HttpStatus.FORBIDDEN;
      case VALIDATION_ERROR, FOLDER_NOT_EMPTY, INVALID_MIME_TYPE -> HttpStatus.BAD_REQUEST;
      case STORAGE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

  @Data
  @Builder
  public static class ErrorResponse {
    private Instant timestamp;
    private String code;
    private String message;
    private String path;
  }
}
