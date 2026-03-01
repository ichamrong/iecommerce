package com.chamrong.iecommerce.invoice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable audit trail entry for a significant lifecycle action on an {@link Invoice}.
 *
 * <p>Entries are append-only — never updated. Used both for internal audit and for the {@code GET
 * /invoices/{id}/audit-log} endpoint.
 *
 * <p>ASVS V8.2 — Critical business transactions are logged with actor and timestamp.
 */
@Entity
@Table(name = "invoice_audit_log")
public class InvoiceAuditEntry {

  /** Discriminator for audit actions. */
  public enum Action {
    CREATED,
    LINE_ADDED,
    LINE_REMOVED,
    ISSUED,
    VOIDED,
    PAID,
    OVERDUE_FLAGGED,
    EMAIL_SENT,
    EMAIL_FAILED
  }

  /** Required by JPA — not for application use. */
  protected InvoiceAuditEntry() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "invoice_id", nullable = false, updatable = false)
  private Long invoiceId;

  @Column(name = "tenant_id", nullable = false, updatable = false, length = 100)
  private String tenantId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false, length = 30)
  private Action action;

  /**
   * Authenticated user or system actor that triggered this action.
   *
   * <p>May be {@code "SYSTEM"} for automated transitions (e.g., OVERDUE).
   */
  @Column(name = "actor_id", nullable = false, updatable = false, length = 255)
  private String actorId;

  /**
   * Optional JSON payload with action-specific context (e.g., void reason, payment reference).
   * Never contains PII or key material.
   */
  @Column(columnDefinition = "TEXT", updatable = false)
  private String details;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private Instant occurredAt;

  /**
   * Factory: creates an immutable audit entry.
   *
   * @param invoiceId the invoice affected
   * @param tenantId tenant scope
   * @param action lifecycle action that occurred
   * @param actorId authenticated user ID (or "SYSTEM")
   * @param details optional JSON context — must NOT contain PII or secrets
   * @param occurredAt moment the action occurred
   */
  public static InvoiceAuditEntry of(
      Long invoiceId,
      String tenantId,
      Action action,
      String actorId,
      String details,
      Instant occurredAt) {
    Objects.requireNonNull(invoiceId, "invoiceId must not be null");
    Objects.requireNonNull(tenantId, "tenantId must not be null");
    Objects.requireNonNull(action, "action must not be null");
    Objects.requireNonNull(actorId, "actorId must not be null");
    Objects.requireNonNull(occurredAt, "occurredAt must not be null");

    InvoiceAuditEntry entry = new InvoiceAuditEntry();
    entry.invoiceId = invoiceId;
    entry.tenantId = tenantId;
    entry.action = action;
    entry.actorId = actorId;
    entry.details = details;
    entry.occurredAt = occurredAt;
    return entry;
  }

  // ── Accessors ──────────────────────────────────────────────────────────────

  public Long getId() {
    return id;
  }

  public Long getInvoiceId() {
    return invoiceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Action getAction() {
    return action;
  }

  public String getActorId() {
    return actorId;
  }

  public String getDetails() {
    return details;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }
}
