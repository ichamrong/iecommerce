package com.chamrong.iecommerce.invoice.domain.exception;

/** Thrown when a signing or verification operation fails. */
public class InvoiceSignatureException extends RuntimeException {

  public InvoiceSignatureException(String message) {
    super(message);
  }

  public InvoiceSignatureException(String message, Throwable cause) {
    super(message, cause);
  }
}
