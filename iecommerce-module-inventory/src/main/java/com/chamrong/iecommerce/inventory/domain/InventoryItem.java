package com.chamrong.iecommerce.inventory.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;

/**
 * Aggregate root representing on-hand stock for a product–warehouse pair.
 *
 * <p>This is the <em>projection</em> entity — a denormalized, read-optimized view updated
 * transactionally every time a {@link StockLedgerEntry} is appended. It never holds the source of
 * truth for historical movements; {@link StockLedgerEntry} does.
 *
 * <p>Concurrency is managed with both pessimistic locking (via {@code SELECT ... FOR UPDATE} in the
 * repository) and optimistic locking ({@code @Version}) as a safety net. The pessimistic lock
 * prevents concurrent reservations from racing past the available-qty guard; the version column
 * catches any unexpected concurrent write that bypasses the row lock.
 *
 * <p>Invariants enforced here (domain layer — no Spring):
 *
 * <ul>
 *   <li>On-hand qty cannot go below 0 after adjustment (unless forced).
 *   <li>Reserved qty cannot exceed on-hand qty.
 *   <li>Available qty = on_hand − reserved (always recomputed, never stored).
 * </ul>
 */
@Getter
@Entity
@Table(
    name = "inventory_stock_level",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
public class InventoryItem extends BaseTenantEntity {

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "warehouse_id", nullable = false)
  private Long warehouseId;

  /** Physical units on-hand (received − committed sales). */
  @Column(name = "on_hand_qty", nullable = false)
  private int onHandQty = 0;

  /** Units currently held by open reservations. */
  @Column(name = "reserved_qty", nullable = false)
  private int reservedQty = 0;

  /** Optimistic lock — caught by JPA before flush. */
  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;

  // ── Factory ──────────────────────────────────────────────────────────────

  public static InventoryItem create(String tenantId, Long productId, Long warehouseId) {
    var item = new InventoryItem();
    item.setTenantId(tenantId);
    item.productId = productId;
    item.warehouseId = warehouseId;
    return item;
  }

  // ── Domain methods ────────────────────────────────────────────────────────

  /**
   * Applies an inbound stock receipt (positive delta only).
   *
   * @param qty units received; must be positive
   */
  public void applyReceipt(int qty) {
    requirePositive(qty, "receipt qty");
    this.onHandQty += qty;
  }

  /**
   * Applies a stock adjustment (positive or negative delta).
   *
   * @param delta signed delta; negative means shrinkage/write-off
   * @param allowNegative if false, throws when result would be negative; use true for forced
   *     corrections only
   */
  public void applyAdjustment(int delta, boolean allowNegative) {
    int next = this.onHandQty + delta;
    if (!allowNegative && next < 0) {
      throw new IllegalArgumentException(
          "Adjustment would result in negative on-hand stock: " + next);
    }
    this.onHandQty = next;
  }

  /**
   * Increments reserved qty after a reservation is created. Called <em>only</em> by {@code
   * ReserveStockHandler} while holding the row lock.
   *
   * @param qty units to reserve
   * @throws OutOfStockException if available qty is insufficient
   */
  public void incrementReserved(int qty) {
    requirePositive(qty, "reserve qty");
    if (getAvailableQty() < qty) {
      throw new OutOfStockException(productId, qty, getAvailableQty());
    }
    this.reservedQty += qty;
  }

  /**
   * Decrements reserved qty when a reservation is released or expired.
   *
   * @param qty units to release; must not exceed current reserved
   */
  public void decrementReserved(int qty) {
    requirePositive(qty, "release qty");
    if (this.reservedQty < qty) {
      throw new InsufficientReservationException(productId, qty, reservedQty);
    }
    this.reservedQty -= qty;
  }

  /**
   * Commits a reservation: decrements both reservedQty and onHandQty simultaneously. Represents
   * permanent sale deduction (order shipped / confirmed).
   *
   * @param qty units committed
   */
  public void commitReservation(int qty) {
    requirePositive(qty, "commit qty");
    if (this.reservedQty < qty) {
      throw new InsufficientReservationException(productId, qty, reservedQty);
    }
    if (this.onHandQty < qty) {
      throw new IllegalStateException(
          "Cannot commit: on-hand qty " + onHandQty + " < commit qty " + qty);
    }
    this.reservedQty -= qty;
    this.onHandQty -= qty;
  }

  /** Sets absolute on-hand qty (used for inventory count reconciliation). */
  public void setOnHandQty(int qty) {
    if (qty < 0) throw new IllegalArgumentException("on_hand_qty cannot be negative: " + qty);
    this.onHandQty = qty;
  }

  /** Setter used by repo adapter during creation. */
  public void setProductId(Long productId) {
    this.productId = productId;
  }

  /** Setter used by repo adapter during creation. */
  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  /** Available = on-hand − reserved. This is always computed, never stored. */
  public int getAvailableQty() {
    return onHandQty - reservedQty;
  }

  // ── Backward-compat aliases (preserves existing InventoryService callers) ─

  /**
   * @deprecated Use {@link #onHandQty}
   */
  @Deprecated(since = "v17")
  public int getQuantity() {
    return onHandQty;
  }

  /**
   * @deprecated Use {@link #setOnHandQty(int)}
   */
  @Deprecated(since = "v17")
  public void setQuantity(int qty) {
    setOnHandQty(qty);
  }

  /**
   * @deprecated Use {@link #reservedQty}
   */
  @Deprecated(since = "v17")
  public int getReservedQuantity() {
    return reservedQty;
  }

  /**
   * @deprecated Use {@link #getAvailableQty()}
   */
  @Deprecated(since = "v17")
  public int getAvailableQuantity() {
    return getAvailableQty();
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private static void requirePositive(int qty, String label) {
    if (qty <= 0) throw new IllegalArgumentException(label + " must be positive, got: " + qty);
  }
}
