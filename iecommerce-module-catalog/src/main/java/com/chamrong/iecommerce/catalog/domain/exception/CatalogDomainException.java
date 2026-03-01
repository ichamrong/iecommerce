package com.chamrong.iecommerce.catalog.domain.exception;

/**
 * Base exception for catalog domain rule violations.
 *
 * <p>Use for duplicate slug/SKU/barcode, invalid state transitions, or validation failures.
 * API layer maps to 400/409 as appropriate.
 */
public class CatalogDomainException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CatalogDomainException(String message) {
    super(message);
  }

  public CatalogDomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
