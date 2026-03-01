package com.chamrong.iecommerce.invoice.domain;

/**
 * Structured log event name constants for the invoice bounded context.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * log.info("[{}] invoiceId={} tenant={}", InvoiceLogEvents.INVOICE_ISSUED, id, tenant);
 * }</pre>
 *
 * <p>These constants are intentionally in the domain layer so all layers can reference them without
 * circular dependencies.
 *
 * <p>ASVS V7.1.1 — Log structure: using named events enables log-aggregation queries (e.g., Kibana:
 * {@code event:"INVOICE_EMAIL_FAILED"}) without embedding magic strings in production code.
 */
public final class InvoiceLogEvents {

  /** A new DRAFT invoice was created. */
  public static final String INVOICE_DRAFT_CREATED = "INVOICE_DRAFT_CREATED";

  /** An invoice transitioned from DRAFT to ISSUED. */
  public static final String INVOICE_ISSUED = "INVOICE_ISSUED";

  /** An invoice was digitally signed (Ed25519). */
  public static final String INVOICE_SIGNED = "INVOICE_SIGNED";

  /** A PDF was generated for an invoice. */
  public static final String INVOICE_PDF_GENERATED = "INVOICE_PDF_GENERATED";

  /** A signed invoice PDF was successfully delivered by email. */
  public static final String INVOICE_EMAIL_SENT = "INVOICE_EMAIL_SENT";

  /** An email delivery attempt failed (will be retried via outbox backoff). */
  public static final String INVOICE_EMAIL_FAILED = "INVOICE_EMAIL_FAILED";

  /** A signature verification attempt on an invoice succeeded. */
  public static final String INVOICE_VERIFIED = "INVOICE_VERIFIED";

  /** A signature verification attempt on an invoice failed. */
  public static final String INVOICE_VERIFICATION_FAILED = "INVOICE_VERIFICATION_FAILED";

  /** An invoice was voided with a mandatory reason. */
  public static final String INVOICE_VOIDED = "INVOICE_VOIDED";

  /** An invoice was marked as paid. */
  public static final String INVOICE_PAID = "INVOICE_PAID";

  private InvoiceLogEvents() {
    // utility class — no instantiation
  }
}
