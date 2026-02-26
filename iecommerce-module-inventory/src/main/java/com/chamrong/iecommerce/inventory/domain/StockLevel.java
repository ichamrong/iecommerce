package com.chamrong.iecommerce.inventory.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(
    name = "inventory_stock_level",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
public class StockLevel extends BaseTenantEntity {

  // Package-private setters for JPA/Infra if needed, but prefer domain methods
  @Setter
  @Column(nullable = false)
  private Long productId;

  @Setter
  @Column(nullable = false)
  private Long warehouseId;

  @Column(nullable = false)
  private Integer quantity = 0;

  @Column(nullable = false)
  private Integer reservedQuantity = 0;

  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;

  // ── Domain Logic ─────────────────────────────────────────────────────────

  /** High-level reservation guard. Validates available stock before incrementing reservation. */
  public void reserve(int qty) {
    if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
    if (getAvailableQuantity() < qty) {
      throw new OutOfStockException(productId);
    }
    this.reservedQuantity += qty;
  }

  public void release(int qty) {
    if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
    if (this.reservedQuantity < qty) {
      throw new IllegalStateException("Cannot release more than reserved quantity");
    }
    this.reservedQuantity -= qty;
  }

  /** Finalizes the sale by deducting from both physical quantity and reservation. */
  public void deduct(int qty) {
    if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
    if (this.reservedQuantity < qty) {
      throw new IllegalStateException("Cannot deduct without matching reservation");
    }
    if (this.quantity < qty) {
      throw new IllegalStateException("Insufficient physical stock for deduction");
    }
    this.reservedQuantity -= qty;
    this.quantity -= qty;
  }

  /** Direct deduction (POS/Manual) without reservation. */
  public void deductInstantly(int qty) {
    if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
    if (getAvailableQuantity() < qty) {
      throw new OutOfStockException(productId);
    }
    this.quantity -= qty;
  }

  public void setQuantity(int qty) {
    if (qty < 0) throw new IllegalArgumentException("Quantity cannot be negative");
    this.quantity = qty;
  }

  public void setTenantId(String tenantId) {
    super.setTenantId(tenantId);
  }

  public int getAvailableQuantity() {
    return quantity - reservedQuantity;
  }
}
