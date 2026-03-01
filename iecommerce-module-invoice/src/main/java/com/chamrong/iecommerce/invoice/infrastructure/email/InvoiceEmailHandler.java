package com.chamrong.iecommerce.invoice.infrastructure.email;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceAuditEntry;
import com.chamrong.iecommerce.invoice.domain.InvoiceAuditEntry.Action;
import com.chamrong.iecommerce.invoice.domain.InvoiceLogEvents;
import com.chamrong.iecommerce.invoice.domain.InvoiceSignature;
import com.chamrong.iecommerce.invoice.domain.ports.EmailSenderPort;
import com.chamrong.iecommerce.invoice.domain.ports.EmailSenderPort.EmailDeliveryException;
import com.chamrong.iecommerce.invoice.domain.ports.EmailSenderPort.InvoiceEmailRequest;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceAuditRepositoryPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceEmailDeliveryPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoicePdfRendererPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceRepositoryPort;
import com.chamrong.iecommerce.invoice.domain.ports.InvoiceSignatureRepositoryPort;
import com.chamrong.iecommerce.invoice.infrastructure.email.InvoiceEmailDelivery.MessageType;
import com.chamrong.iecommerce.invoice.infrastructure.email.InvoiceEmailDelivery.Status;
import com.chamrong.iecommerce.invoice.infrastructure.persistence.jpa.InvoiceOutboxRelay.InvoiceIssuedPayload;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox consumer: listens for {@link InvoiceIssuedPayload} application events and delivers the
 * signed invoice PDF by email.
 *
 * <h2>Idempotency</h2>
 *
 * Before attempting to send, this handler checks whether a {@link InvoiceEmailDelivery} record for
 * this invoice + message type already exists with {@code status == SENT}. If so, it does nothing —
 * ensuring exactly-once delivery semantics even if the outbox event is replayed.
 *
 * <h2>Error handling</h2>
 *
 * On SMTP failure, the delivery record is marked {@code FAILED} and an audit entry is appended. The
 * outbox relay will re-publish the event after the backoff window, at which point this handler will
 * retry (since {@code status == FAILED}, not {@code SENT}).
 *
 * <h2>ASVS</h2>
 *
 * <ul>
 *   <li>V14.5 — Idempotent event consumption: SENT records are never re-sent.
 *   <li>V7.1.1 — Recipient email is masked in all log output via {@link
 *       InvoiceEmailDelivery#maskedEmail()}.
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceEmailHandler {

  private static final String SYSTEM_ACTOR = "SYSTEM";

  private final InvoiceRepositoryPort invoiceRepository;
  private final InvoiceSignatureRepositoryPort signatureRepository;
  private final InvoicePdfRendererPort pdfRenderer;
  private final EmailSenderPort emailSender;
  private final InvoiceAuditRepositoryPort auditRepository;
  private final InvoiceEmailDeliveryPort emailDeliveryPort;

  @Value("${invoice.email.verification-base-url:http://localhost:8080/api/v1}")
  private String verificationBaseUrl;

  @Value("${invoice.email.enabled:true}")
  private boolean emailEnabled;

  /**
   * Handles the {@link InvoiceIssuedPayload} event published by the outbox relay.
   *
   * <p>This method is invoked in a new transaction so that the delivery record and audit entry are
   * committed atomically with the email result — independent of the relay's transaction.
   *
   * @param payload the issued invoice event payload
   */
  @EventListener
  @Transactional
  public void onInvoiceIssued(InvoiceIssuedPayload payload) {
    if (!emailEnabled) {
      log.debug(
          "Email sending disabled via invoice.email.enabled=false — skipping invoiceId={}",
          payload.invoiceId());
      return;
    }

    String idempotencyKey = payload.invoiceId() + ":" + MessageType.INVOICE_ISSUED.name();

    // ── Step 1: idempotency check ──────────────────────────────────────────
    InvoiceEmailDelivery delivery =
        emailDeliveryPort.findByIdempotencyKey(idempotencyKey).orElse(null);

    if (delivery != null && delivery.getStatus() == Status.SENT) {
      log.debug(
          "[{}] Duplicate event for invoiceId={} — already sent, skipping",
          InvoiceLogEvents.INVOICE_EMAIL_SENT,
          payload.invoiceId());
      return;
    }

    // ── Step 2: resolve invoice + email address ────────────────────────────
    Invoice invoice =
        invoiceRepository.findByIdAndTenant(payload.invoiceId(), payload.tenantId()).orElse(null);

    if (invoice == null) {
      log.warn(
          "[{}] invoiceId={} not found for tenant={} — cannot send email",
          InvoiceLogEvents.INVOICE_EMAIL_FAILED,
          payload.invoiceId(),
          payload.tenantId());
      return;
    }

    // Derive recipient: buyerSnapshot should contain "email" field; fallback to no-email state
    String recipientEmail = extractRecipientEmail(invoice);
    if (recipientEmail == null || recipientEmail.isBlank()) {
      log.warn(
          "[{}] invoiceId={} has no recipient email in buyerSnapshot — skipping",
          InvoiceLogEvents.INVOICE_EMAIL_FAILED,
          payload.invoiceId());
      return;
    }

    // ── Step 3: create or reuse delivery record ────────────────────────────
    Instant now = Instant.now();
    if (delivery == null) {
      delivery =
          InvoiceEmailDelivery.pending(
              payload.invoiceId(),
              payload.tenantId(),
              recipientEmail,
              MessageType.INVOICE_ISSUED,
              now);
      delivery = emailDeliveryPort.save(delivery);
    }

    // ── Step 4: generate PDF ───────────────────────────────────────────────
    InvoiceSignature signature =
        signatureRepository.findByInvoiceId(payload.invoiceId()).orElse(null);

    if (signature == null) {
      String reason = "No signature found for invoiceId=" + payload.invoiceId();
      delivery.markFailed(reason, now);
      emailDeliveryPort.save(delivery);
      appendAudit(invoice, Action.EMAIL_FAILED, reason, now);
      return;
    }

    byte[] pdfBytes;
    try {
      pdfBytes = pdfRenderer.render(invoice, signature);
      log.debug(
          "[{}] invoiceId={} pdf={}bytes",
          InvoiceLogEvents.INVOICE_PDF_GENERATED,
          invoice.getId(),
          pdfBytes.length);
    } catch (Exception ex) {
      String reason = "PDF generation failed: " + ex.getClass().getSimpleName();
      delivery.markFailed(reason, now);
      emailDeliveryPort.save(delivery);
      appendAudit(invoice, Action.EMAIL_FAILED, reason, now);
      return;
    }

    // ── Step 5: build verify URL ───────────────────────────────────────────
    String verifyUrl =
        verificationBaseUrl.stripTrailing() + "/invoices/" + invoice.getId() + "/verify";

    // ── Step 6: send email ─────────────────────────────────────────────────
    InvoiceEmailRequest request =
        new InvoiceEmailRequest(
            recipientEmail,
            invoice.getInvoiceNumber(),
            invoice.getId(),
            verifyUrl,
            pdfBytes,
            payload.tenantId());

    try {
      emailSender.sendInvoiceEmail(request);
      delivery.markSent(now);
      emailDeliveryPort.save(delivery);
      appendAudit(
          invoice, Action.EMAIL_SENT, "{\"recipient\":\"" + delivery.maskedEmail() + "\"}", now);
      log.info(
          "[{}] invoiceId={} tenant={} recipient={}",
          InvoiceLogEvents.INVOICE_EMAIL_SENT,
          invoice.getId(),
          payload.tenantId(),
          delivery.maskedEmail());
    } catch (EmailDeliveryException ex) {
      // Exception message must not contain PII (enforced by EmailSenderPort contract)
      delivery.markFailed(ex.getMessage(), now);
      emailDeliveryPort.save(delivery);
      appendAudit(
          invoice, Action.EMAIL_FAILED, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}", now);
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  /**
   * Attempts to extract the recipient email from the invoice's buyer snapshot JSON. The snapshot is
   * a free-form JSON string; we look for {@code "email":"..."}.
   */
  private String extractRecipientEmail(Invoice invoice) {
    String snapshot = invoice.getBuyerSnapshot();
    if (snapshot == null) return null;
    // Simple extraction — avoids pulling Jackson for a single field
    int idx = snapshot.indexOf("\"email\"");
    if (idx < 0) return null;
    int colon = snapshot.indexOf(':', idx);
    if (colon < 0) return null;
    int start = snapshot.indexOf('"', colon + 1);
    if (start < 0) return null;
    int end = snapshot.indexOf('"', start + 1);
    if (end < 0) return null;
    return snapshot.substring(start + 1, end);
  }

  private void appendAudit(Invoice invoice, Action action, String details, Instant now) {
    try {
      auditRepository.append(
          InvoiceAuditEntry.of(
              invoice.getId(), invoice.getTenantId(), action, SYSTEM_ACTOR, details, now));
    } catch (Exception ex) {
      log.warn(
          "Failed to append audit entry for invoiceId={}: {}", invoice.getId(), ex.getMessage());
    }
  }

  private static String escapeJson(String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
