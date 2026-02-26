package com.chamrong.iecommerce.order.domain;

import java.util.List;

/**
 * Saga Event: Fired when an order is shipped. Inventory module listens to this to permanently
 * deduct stock.
 */
public record OrderShippedEvent(Long orderId, String tenantId, List<Item> items) {
  public record Item(Long productVariantId, int quantity) {}
}
