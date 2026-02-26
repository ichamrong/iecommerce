package com.chamrong.iecommerce.common.event;

import java.util.List;

public record OrderConfirmedEvent(
    Long orderId, String tenantId, Long customerId, List<Item> items) {
  public record Item(Long productVariantId, int quantity) {}
}
