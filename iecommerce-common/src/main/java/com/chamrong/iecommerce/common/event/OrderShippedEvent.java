package com.chamrong.iecommerce.common.event;

import java.util.List;

public record OrderShippedEvent(
    Long orderId, String tenantId, Long customerId, String trackingNumber, List<Item> items) {
  public record Item(Long productVariantId, int quantity) {}
}
