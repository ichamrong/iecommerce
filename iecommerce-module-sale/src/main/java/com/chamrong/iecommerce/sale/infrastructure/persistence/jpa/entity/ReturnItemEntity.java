package com.chamrong.iecommerce.sale.infrastructure.persistence.jpa.entity;

import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sale_return_items")
@Getter
@Setter
public class ReturnItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sale_return_id", nullable = false)
  private SaleReturnEntity saleReturn;

  @Column(nullable = false)
  private Long originalLineId;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal quantity;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "refund_price")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money refundPrice;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "total_refund_amount")),
    @AttributeOverride(
        name = "currency",
        column = @Column(name = "currency", insertable = false, updatable = false))
  })
  private Money totalRefundAmount;
}
