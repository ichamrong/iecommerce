package com.chamrong.iecommerce.sale.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sales_return_items")
@Getter
@Setter
public class ReturnItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sale_return_id")
  private SaleReturn saleReturn;

  @Column(nullable = false)
  private String productId;

  @Column(nullable = false)
  private BigDecimal quantity;

  private String condition; // e.g., "UNOPENED", "DAMAGED"
}
