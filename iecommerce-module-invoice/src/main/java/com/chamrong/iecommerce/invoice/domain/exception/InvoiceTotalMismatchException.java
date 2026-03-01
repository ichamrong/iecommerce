package com.chamrong.iecommerce.invoice.domain.exception;

import java.math.BigDecimal;

/** Thrown when the declared invoice total does not equal the sum of its line items. */
public class InvoiceTotalMismatchException extends RuntimeException {

  public InvoiceTotalMismatchException(BigDecimal expected, BigDecimal actual) {
    super("Invoice total mismatch: declared=" + expected + ", computed=" + actual);
  }
}
