package com.chamrong.iecommerce.auth.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode {
  USER_NOT_FOUND("AUTH-001", "User not found", HttpStatus.NOT_FOUND),
  INVALID_CREDENTIALS("AUTH-002", "Invalid credentials", HttpStatus.UNAUTHORIZED),
  USER_DISABLED("AUTH-003", "User account is disabled", HttpStatus.FORBIDDEN),
  UNAUTHORIZED_ACCESS("AUTH-004", "Unauthorized access", HttpStatus.FORBIDDEN),
  DUPLICATE_USER("AUTH-005", "User already exists", HttpStatus.CONFLICT),
  TENANT_NOT_FOUND("AUTH-006", "Tenant not found", HttpStatus.NOT_FOUND),
  TENANT_INACTIVE("AUTH-007", "Tenant is inactive", HttpStatus.FORBIDDEN),
  DUPLICATE_TENANT("AUTH-008", "Tenant already exists", HttpStatus.CONFLICT),
  INTERNAL_ERROR("AUTH-009", "Internal authentication error", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_TOKEN("AUTH-010", "Invalid or expired token", HttpStatus.UNAUTHORIZED);

  private final String code;
  private final String message;
  private final HttpStatus status;

  AuthErrorCode(String code, String message, HttpStatus status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }
}
