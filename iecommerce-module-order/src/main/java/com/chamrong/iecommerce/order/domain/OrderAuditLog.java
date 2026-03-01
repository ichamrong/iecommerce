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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Immutable audit log entry for every Order state transition.
 *
 * <p>This follows the banking-grade "append-only ledger" pattern. Rows are NEVER updated or deleted
 * — only inserted. This provides a full, tamper-evident history of every change to every order.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
  @Column(nullable = true, length = 50)
  private OrderState fromState;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private OrderState toState;

  @Column(nullable = false, length = 100)
  private String action;

  @Column(length = 255)
  private String performedBy;

  @Column(columnDefinition = "TEXT")
  private String context;

  @Builder.Default
  @Column(nullable = false, updatable = false)
  private Instant occurredAt = Instant.now();

  /** Standard constructor for JpaOrderAuditAdapter. */
  public OrderAuditLog(
      Long orderId,
      String tenantId,
      OrderState from,
      OrderState to,
      String action,
      String performedBy,
      String context) {
    this.orderId = orderId;
    this.tenantId = tenantId;
    this.fromState = from;
    this.toState = to;
    this.action = action;
    this.performedBy = performedBy;
    this.context = context;
    this.occurredAt = Instant.now();
  }
}
