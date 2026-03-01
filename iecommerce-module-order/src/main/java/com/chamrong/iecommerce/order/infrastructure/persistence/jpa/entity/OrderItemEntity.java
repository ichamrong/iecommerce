package com.chamrong.iecommerce.order.infrastructure.persistence.jpa.entity;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for order line item. Domain model is {@link
 * com.chamrong.iecommerce.order.domain.OrderItem}.
 */
@Entity
@Table(name = "ecommerce_order_item")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemEntity extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private OrderEntity order;

  @Column(name = "product_variant_id")
  private Long productVariantId;

  @Column(nullable = false)
  private Integer quantity;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
  })
  private Money unitPrice;

  private Instant startAt;
  private Instant endAt;
}
