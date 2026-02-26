package com.chamrong.iecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Immutable audit log entry for every Order state transition.
 *
 * <p>This follows the banking-grade "append-only ledger" pattern. Rows are NEVER updated or deleted
 * — only inserted. This provides a full, tamper-evident history of every change to every order.
 *
 * <p>Combined with PostgreSQL WAL (Write-Ahead Log), this ensures that even if a server crashes
 * mid-write, no committed audit record is ever lost.
 */
@Entity
@Table(name = "order_audit_log")
public class OrderAuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false, length = 100)
  private String tenantId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private OrderState fromState;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private OrderState toState;

  /** The event/action that caused this transition. e.g., "ORDER_CONFIRMED", "ITEM_ADDED" */
  @Column(nullable = false, length = 100)
  private String action;

  /**
   * The principal (user ID or system identifier) who performed the action. Populated from the JWT
   * subject claim.
   */
  @Column(length = 255)
  private String performedBy;

  /** Snapshot of relevant context — e.g. item count, total amount at time of event. */
  @Column(columnDefinition = "TEXT")
  private String context;

  @Column(nullable = false, updatable = false)
  private Instant occurredAt = Instant.now();

  // ── Factory methods ───────────────────────────────────────────────────────

  public static OrderAuditLog of(
      Long orderId,
      String tenantId,
      OrderState from,
      OrderState to,
      String action,
      String performedBy,
      String context) {
    OrderAuditLog log = new OrderAuditLog();
    log.orderId = orderId;
    log.tenantId = tenantId;
    log.fromState = from;
    log.toState = to;
    log.action = action;
    log.performedBy = performedBy;
    log.context = context;
    log.occurredAt = Instant.now();
    return log;
  }

  // ── Getters (no setters — immutable record) ───────────────────────────────

  public Long getId() {
    return id;
  }

  public Long getOrderId() {
    return orderId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public OrderState getFromState() {
    return fromState;
  }

  public OrderState getToState() {
    return toState;
  }

  public String getAction() {
    return action;
  }

  public String getPerformedBy() {
    return performedBy;
  }

  public String getContext() {
    return context;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }
}
