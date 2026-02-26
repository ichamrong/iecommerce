package com.chamrong.iecommerce.invoice.domain;

/** Lifecycle status of an invoice. */
public enum InvoiceStatus {
  /** Created but not yet formally sent to the customer. */
  DRAFT,
  /** Issued and sent to the customer — awaiting payment. */
  ISSUED,
  /** Payment received in full. */
  PAID,
  /** Cancelled / reversed — no payment expected. */
  VOID
}
