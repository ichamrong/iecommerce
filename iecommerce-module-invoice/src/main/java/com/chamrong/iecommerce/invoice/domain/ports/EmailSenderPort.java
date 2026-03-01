package com.chamrong.iecommerce.invoice.domain.ports;

/**
 * Outbound port: delivers a signed invoice PDF to a recipient via email.
 *
 * <p>Implementations MUST:
 *
 * <ul>
 *   <li>Attach the signed PDF as {@code application/pdf}.
 *   <li>Include a "Verify Invoice" hyperlink in the email body.
 *   <li>Mask recipient addresses in all log output (ASVS V7.1.1 — no PII in logs).
 *   <li>Throw {@link EmailDeliveryException} on any SMTP-level failure so the caller can record the
 *       failure, apply backoff, and retry via the outbox.
 * </ul>
 *
 * <p>Callers MUST be idempotent: they must check delivery status before calling this port to avoid
 * sending duplicate emails (see {@code InvoiceEmailHandler}).
 */
public interface EmailSenderPort {

  /**
   * Sends a signed invoice email.
   *
   * @param request all data needed to compose and send the email
   * @throws EmailDeliveryException on SMTP-level failure (caller retries via outbox backoff)
   */
  void sendInvoiceEmail(InvoiceEmailRequest request);

  // ── Value object ─────────────────────────────────────────────────────────

  /**
   * Immutable value object carrying all data needed to compose an invoice email.
   *
   * @param toAddress recipient email address — masked in logs as {@code X**@domain}
   * @param invoiceNumber human-readable invoice identifier, e.g. {@code ACME-2026-000042}
   * @param invoiceId internal invoice ID used to construct the verify URL
   * @param verifyUrl full verification URL embedded in the email body
   * @param pdfBytes raw bytes of the signed PDF attachment
   * @param tenantId calling tenant (for logging context; never logged as PII)
   */
  record InvoiceEmailRequest(
      String toAddress,
      String invoiceNumber,
      Long invoiceId,
      String verifyUrl,
      byte[] pdfBytes,
      String tenantId) {}

  // ── Port-specific exception ───────────────────────────────────────────────

  /**
   * Thrown when an SMTP-level failure prevents email delivery.
   *
   * <p>The message MUST NOT contain the recipient address (ASVS V7.1.1). Use a sanitized message
   * such as {@code "SMTP send failed: Connection refused"}.
   */
  class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
      super(message, cause);
    }

    public EmailDeliveryException(String message) {
      super(message);
    }
  }
}
