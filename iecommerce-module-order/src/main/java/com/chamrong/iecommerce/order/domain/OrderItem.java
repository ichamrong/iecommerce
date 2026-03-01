package com.chamrong.iecommerce.order.domain;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.domain.BaseDomainEntity;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Order line item (pure domain — no JPA). Persistence uses OrderItemEntity in infrastructure. */
@Getter
@Setter
public class OrderItem extends BaseDomainEntity {

  private Order order;
  private Long productVariantId;
  private Integer quantity;
  private Money unitPrice;
  private Instant startAt;
  private Instant endAt;

  /** Called by {@link Order#addItem(OrderItem)} and by mapper during restore. */
  public void assignOrder(Order order) {
    this.order = order;
  }

  public static OrderItem of(
      Long productVariantId, Integer quantity, Money unitPrice, Instant startAt, Instant endAt) {
    OrderItem item = new OrderItem();
    item.productVariantId = productVariantId;
    item.quantity = quantity;
    item.unitPrice = unitPrice;
    item.startAt = startAt;
    item.endAt = endAt;
    return item;
  }

  public void updateQuantity(int newQuantity) {
    this.quantity = newQuantity;
  }
}
