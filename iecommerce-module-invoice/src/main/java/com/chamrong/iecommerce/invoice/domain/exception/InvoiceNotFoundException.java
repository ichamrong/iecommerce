package com.chamrong.iecommerce.invoice.domain.exception;

/** Thrown when an invoice cannot be found for the given tenant + ID combination. */
public class InvoiceNotFoundException extends RuntimeException {

  public InvoiceNotFoundException(Long invoiceId, String tenantId) {
    super("Invoice not found: id=" + invoiceId + ", tenant=" + tenantId);
  }

  public InvoiceNotFoundException(String message) {
    super(message);
  }
}
