package com.chamrong.iecommerce.inventory.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inventory_stock_movement")
public class StockMovement extends BaseTenantEntity {

  @Column(nullable = false)
  private Long productId;

  @Column(nullable = false)
  private Long warehouseId;

  @Column(nullable = false)
  private Integer quantity; // Negative for reduction, positive for addition

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private MovementReason reason;

  @Column(nullable = false)
  private Instant occurrenceDate = Instant.now();

  private String comment;

  public enum MovementReason {
    RESTOCK,
    SALE,
    DAMAGED,
    EXPIRED,
    RETURNED,
    CORRECTION
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
}
