package com.chamrong.iecommerce.inventory.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

  public enum MovementReason {
    RESTOCK,
    SALE,
    DAMAGED,
    EXPIRED,
    RETURNED,
    CORRECTION
  }
}
