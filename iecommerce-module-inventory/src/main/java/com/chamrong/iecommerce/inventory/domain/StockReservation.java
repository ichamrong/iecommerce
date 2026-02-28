package com.chamrong.iecommerce.inventory.domain;

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

/**
 * Represents a stock hold placed by a specific business operation (e.g., an order line).
 *
 * <p>Lifecycle: {@code PENDING → COMMITTED | RELEASED | EXPIRED}
 *
 * <p>The unique constraint {@code uq_reservation_ref(tenant_id, reference_type, reference_id)}
 * enforces idempotency at the DB level — a second attempt to reserve with the same external
 * reference will fail with a constraint violation, which the application layer catches and converts
 * to a silent idempotent return.
 *
 * <p>Table: {@code inventory_stock_reservation}
 */
@Getter
@Entity
@Table(
    name = "inventory_stock_reservation",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_reservation_ref",
            columnNames = {"tenant_id", "reference_type", "reference_id"}))
public class StockReservation {

  public enum ReservationStatus {
    PENDING,
    COMMITTED,
    RELEASED,
    EXPIRED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tenant_id", nullable = false, updatable = false, length = 100)
  private String tenantId;

  @Column(name = "product_id", nullable = false, updatable = false)
  private Long productId;

  @Column(name = "warehouse_id", nullable = false, updatable = false)
  private Long warehouseId;

  @Column(name = "qty", nullable = false, updatable = false)
  private int qty;

  /** E.g. "ORDER", "RETURN", "TRANSFER". */
  @Column(name = "reference_type", nullable = false, updatable = false, length = 100)
  private String referenceType;

  /** E.g. orderId, lineItemId — forms composite idempotency key with referenceType. */
  @Column(name = "reference_id", nullable = false, updatable = false, length = 255)
  private String referenceId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ReservationStatus status = ReservationStatus.PENDING;

  /** When null, the reservation never expires (manual operations). */
  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  // ── Factory ──────────────────────────────────────────────────────────────

  private StockReservation() {}

  public static StockReservation create(
      String tenantId,
      Long productId,
      Long warehouseId,
      int qty,
      String referenceType,
      String referenceId,
      Instant expiresAt,
      Instant now) {
    var r = new StockReservation();
    r.tenantId = tenantId;
    r.productId = productId;
    r.warehouseId = warehouseId;
    r.qty = qty;
    r.referenceType = referenceType;
    r.referenceId = referenceId;
    r.expiresAt = expiresAt;
    r.createdAt = now;
    r.updatedAt = now;
    return r;
  }

  // ── Domain FSM ────────────────────────────────────────────────────────────

  /**
   * Commits a PENDING reservation (goods dispatched / order confirmed).
   *
   * @throws IllegalStateException if already committed/released/expired
   */
  public void commit(Instant now) {
    requireStatus(ReservationStatus.PENDING, "commit");
    this.status = ReservationStatus.COMMITTED;
    this.updatedAt = now;
  }

  /**
   * Releases a PENDING reservation (order cancelled by customer).
   *
   * @throws IllegalStateException if not in PENDING state
   */
  public void release(Instant now) {
    requireStatus(ReservationStatus.PENDING, "release");
    this.status = ReservationStatus.RELEASED;
    this.updatedAt = now;
  }

  /**
   * Marks a PENDING reservation as EXPIRED (scheduler-triggered).
   *
   * @throws IllegalStateException if not in PENDING state
   */
  public void expire(Instant now) {
    requireStatus(ReservationStatus.PENDING, "expire");
    this.status = ReservationStatus.EXPIRED;
    this.updatedAt = now;
  }

  public boolean isPending() {
    return status == ReservationStatus.PENDING;
  }

  // ── Helper ────────────────────────────────────────────────────────────────

  private void requireStatus(ReservationStatus expected, String operation) {
    if (this.status != expected) {
      throw new IllegalStateException(
          "Cannot "
              + operation
              + " reservation in status "
              + status
              + "; expected "
              + expected
              + ". referenceId="
              + referenceId);
    }
  }
}
