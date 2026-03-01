package com.chamrong.iecommerce.audit.domain.exception;

/**
 * Base domain exception for audit operations (e.g. validation, policy violation).
 *
 * <p>Use for business rule failures; map to appropriate HTTP status in API layer.
 */
public class AuditDomainException extends RuntimeException {

  private final String errorCode;

  public AuditDomainException(String message) {
    super(message);
    this.errorCode = "AUDIT_ERROR";
  }

  public AuditDomainException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode != null ? errorCode : "AUDIT_ERROR";
  }

  public AuditDomainException(String message, Throwable cause) {
    super(message, cause);
    this.errorCode = "AUDIT_ERROR";
  }

  public String getErrorCode() {
    return errorCode;
  }
}
