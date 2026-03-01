package com.chamrong.iecommerce.invoice.domain;

/**
 * Lifecycle state machine for an {@link Invoice}.
 *
 * <p>Allowed transitions:
 *
 * <pre>
 *   DRAFT ──► ISSUED ──► PAID
 *                └──────► VOIDED
 *   ISSUED ──► OVERDUE ──► PAID
 *                    └───► VOIDED
 * </pre>
 */
public enum InvoiceStatus {

  /** Created; line items may still be added or removed. */
  DRAFT,

  /** Formally issued & signed; content is immutable. */
  ISSUED,

  /** Payment received in full. Terminal state. */
  PAID,

  /** Cancelled with a mandatory reason. Terminal state. */
  VOIDED,

  /**
   * Past due date with no payment. System may transition ISSUED → OVERDUE automatically.
   * Non-terminal: can still be PAID or VOIDED.
   */
  OVERDUE;

  /**
   * @return true when no further business transitions are possible.
   */
  public boolean isTerminal() {
    return this == PAID || this == VOIDED;
  }

  /**
   * @return true when the invoice may be voided from this state.
   */
  public boolean allowsVoid() {
    return this == ISSUED || this == OVERDUE;
  }

  /**
   * @return true when the invoice may be marked paid from this state.
   */
  public boolean allowsPaid() {
    return this == ISSUED || this == OVERDUE;
  }

  /**
   * @return true when further modifications (e.g., adding lines) are allowed.
   */
  public boolean allowsModification() {
    return this == DRAFT;
  }
}
