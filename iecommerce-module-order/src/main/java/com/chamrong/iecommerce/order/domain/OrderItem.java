package com.chamrong.iecommerce.order.domain;

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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ecommerce_order_item")
public class OrderItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order;

  /** Cross-boundary reference to catalog's ProductVariant. */
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

  /** For time-based items (bookings, accommodations). */
  private java.time.Instant startAt;

  private java.time.Instant endAt;
}
