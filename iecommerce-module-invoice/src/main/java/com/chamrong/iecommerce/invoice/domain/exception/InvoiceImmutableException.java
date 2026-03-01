package com.chamrong.iecommerce.invoice.domain.exception;

import com.chamrong.iecommerce.invoice.domain.InvoiceStatus;

/**
 * Thrown when an attempt is made to mutate an invoice that has already been issued or transitioned
 * to a terminal state.
 *
 * <p>ASVS V8.3 — Business logic: enforces immutability invariant post-issuance.
 */
public class InvoiceImmutableException extends RuntimeException {

  public InvoiceImmutableException(Long invoiceId, InvoiceStatus current, String operation) {
    super("Cannot perform '" + operation + "' on invoice " + invoiceId + " in status " + current);
  }
}
