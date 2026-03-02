package com.chamrong.iecommerce.report.domain.exception;

/** Base exception for report domain errors. */
public class ReportDomainException extends RuntimeException {

  public ReportDomainException(String message) {
    super(message);
  }

  public ReportDomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
