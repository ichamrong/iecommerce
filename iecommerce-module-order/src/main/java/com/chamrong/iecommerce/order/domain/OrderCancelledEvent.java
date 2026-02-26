package com.chamrong.iecommerce.order.domain;

import java.util.List;

/**
 * Saga Event: Fired when an order is cancelled. Inventory module listens to this to release
 * reserved stock.
 */
public record OrderCancelledEvent(Long orderId, String tenantId, List<Item> items) {
  public record Item(Long productVariantId, int quantity) {}
}
