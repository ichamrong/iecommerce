package com.chamrong.iecommerce.inventory.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "inventory_stock_movement")
public class StockMovement extends BaseTenantEntity {

  @Column(nullable = false)
  private Long productId;

  @Column(nullable = false)
  private Long warehouseId;

  /** Positive = addition, negative = reduction. */
  @Column(nullable = false)
  private Integer quantity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private MovementReason reason;

  @Column(nullable = false)
  private Instant occurrenceDate = Instant.now();

  @Column(columnDefinition = "TEXT")
  private String comment;

  public StockMovement() {}

  public static StockMovement of(
      String tenantId,
      Long productId,
      Long warehouseId,
      Integer quantity,
      MovementReason reason,
      String comment) {
    var m = new StockMovement();
    m.setTenantId(tenantId);
    m.productId = productId;
    m.warehouseId = warehouseId;
    m.quantity = quantity;
    m.reason = reason;
    m.comment = comment;
    m.occurrenceDate = Instant.now();
    return m;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getWarehouseId() {
    return warehouseId;
  }

  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public MovementReason getReason() {
    return reason;
  }

  public void setReason(MovementReason reason) {
    this.reason = reason;
  }

  public Instant getOccurrenceDate() {
    return occurrenceDate;
  }

  public void setOccurrenceDate(Instant occurrenceDate) {
    this.occurrenceDate = occurrenceDate;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public enum MovementReason {
    RESTOCK,
    SALE,
    DAMAGED,
    EXPIRED,
    RETURNED,
    CORRECTION
  }
}
