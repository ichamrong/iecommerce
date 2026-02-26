package com.chamrong.iecommerce.common.event;

import java.util.List;

public record OrderCancelledEvent(
    Long orderId, String tenantId, Long customerId, List<Item> items) {
  public record Item(Long productVariantId, int quantity) {}
}
