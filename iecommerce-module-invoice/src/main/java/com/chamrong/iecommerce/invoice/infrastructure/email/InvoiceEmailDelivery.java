package com.chamrong.iecommerce.invoice.infrastructure.email;

import com.chamrong.iecommerce.invoice.domain.InvoiceLogEvents;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA entity tracking email delivery attempts for each invoice.
 *
 * <h2>Idempotency</h2>
 *
 * The {@code idempotency_key} column has a UNIQUE constraint: {@code {invoiceId}:{messageType}}.
 * This guarantees that the outbox relay can publish the same {@code INVOICE_ISSUED} event multiple
 * times without sending duplicate emails — the handler checks {@code status == SENT} before
 * attempting to send.
 *
 * <h2>ASVS</h2>
 *
 * <ul>
 *   <li>V14.5 — outbox + idempotency key prevents at-least-once delivery duplication.
 *   <li>V7.1.1 — {@code recipientEmail} is stored but masked in all log output.
 * </ul>
 */
@Slf4j
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "invoice_email_delivery",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_email_delivery_idempotency", columnNames = "idempotency_key"))
public class InvoiceEmailDelivery {

  /** Delivery status. */
  public enum Status {
    PENDING,
    SENT,
    FAILED
  }

  /** Message type — used as part of the idempotency key. */
  public enum MessageType {
    INVOICE_ISSUED,
    INVOICE_RESEND
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "invoice_id", nullable = false)
  private Long invoiceId;

  @Column(name = "tenant_id", nullable = false, length = 100)
  private String tenantId;

  /** Stored as-is — MUST be masked in all log output. Use {@link #maskedEmail()} for logging. */
  @Setter
  @Column(name = "recipient_email", nullable = false, length = 255)
  private String recipientEmail;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_type", nullable = false, length = 50)
  private MessageType messageType;

  @Setter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  @Setter
  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Setter
  @Column(name = "last_attempted_at")
  private Instant lastAttemptedAt;

  @Setter
  @Column(name = "sent_at")
  private Instant sentAt;

  /** Sanitized last failure reason — MUST NOT contain PII. */
  @Setter
  @Column(name = "error_message")
  private String errorMessage;

  /**
   * Idempotency key: {@code "{invoiceId}:{messageType}"}. DB UNIQUE constraint prevents duplicate
   * sends.
   */
  @Column(name = "idempotency_key", nullable = false, length = 255, updatable = false)
  private String idempotencyKey;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  // ── Factory ───────────────────────────────────────────────────────────────

  /**
   * Creates a new PENDING delivery record.
   *
   * @param invoiceId invoice being sent
   * @param tenantId owning tenant
   * @param email recipient address (masked in logs)
   * @param type reason for this delivery
   * @param now creation timestamp
   * @return unsaved entity
   */
  public static InvoiceEmailDelivery pending(
      Long invoiceId, String tenantId, String email, MessageType type, Instant now) {
    InvoiceEmailDelivery d = new InvoiceEmailDelivery();
    d.invoiceId = invoiceId;
    d.tenantId = tenantId;
    d.recipientEmail = email;
    d.messageType = type;
    d.status = Status.PENDING;
    d.attemptCount = 0;
    d.idempotencyKey = invoiceId + ":" + type.name();
    d.createdAt = now;
    return d;
  }

  // ── Behaviour ─────────────────────────────────────────────────────────────

  /** Marks this delivery successful. */
  public void markSent(Instant now) {
    this.status = Status.SENT;
    this.sentAt = now;
    this.lastAttemptedAt = now;
    this.errorMessage = null;
    log.info(
        "[{}] invoiceId={} tenant={} recipient={}",
        InvoiceLogEvents.INVOICE_EMAIL_SENT,
        invoiceId,
        tenantId,
        maskedEmail());
  }

  /**
   * Marks this delivery failed and increments the attempt counter. The {@code sanitizedReason} MUST
   * NOT contain PII or SMTP credentials.
   */
  public void markFailed(String sanitizedReason, Instant now) {
    this.status = Status.FAILED;
    this.attemptCount++;
    this.lastAttemptedAt = now;
    this.errorMessage = sanitizedReason;
    log.warn(
        "[{}] invoiceId={} tenant={} attempt={} reason={}",
        InvoiceLogEvents.INVOICE_EMAIL_FAILED,
        invoiceId,
        tenantId,
        attemptCount,
        sanitizedReason);
  }

  /** Re-queues a failed or sent delivery for re-sending. */
  public void requeue(Instant now) {
    this.status = Status.PENDING;
    this.lastAttemptedAt = null;
    this.errorMessage = null;
    // idempotencyKey is immutable — resend handler uses MessageType.INVOICE_RESEND
  }

  /**
   * Returns a PII-safe masked representation of the recipient email for log output. e.g. {@code
   * john.doe@example.com} → {@code joh**@example.com}
   */
  public String maskedEmail() {
    if (recipientEmail == null) return "[null]";
    int at = recipientEmail.indexOf('@');
    if (at < 3) return "**@" + (at >= 0 ? recipientEmail.substring(at + 1) : "?");
    return recipientEmail.substring(0, 3) + "**" + recipientEmail.substring(at);
  }
}

// SpringDataInvoiceEmailDeliveryRepository has been extracted to
// JpaInvoiceEmailDeliveryAdapter.java
