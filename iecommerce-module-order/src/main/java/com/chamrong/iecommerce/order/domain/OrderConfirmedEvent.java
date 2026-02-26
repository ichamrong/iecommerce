package com.chamrong.iecommerce.order.domain;

import java.util.List;

/**
 * Saga Event: Fired when an order is confirmed. Inventory module listens to this to synchronously
 * or asynchronously reserve stock.
 */
public record OrderConfirmedEvent(Long orderId, String tenantId, List<Item> items) {
  public record Item(Long productVariantId, int quantity) {}
}
