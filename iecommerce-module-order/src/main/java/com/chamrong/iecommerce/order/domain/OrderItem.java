package com.chamrong.iecommerce.order.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;

@Entity
@Table(name = "ecommerce_order_item")
public class OrderItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order;

  private Long productVariantId; // Reference by ID to maintain boundary

  private Integer quantity;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
  })
  private Money unitPrice;

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public Long getProductVariantId() {
    return productVariantId;
  }

  public void setProductVariantId(Long productVariantId) {
    this.productVariantId = productVariantId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Money getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(Money unitPrice) {
    this.unitPrice = unitPrice;
  }
}
