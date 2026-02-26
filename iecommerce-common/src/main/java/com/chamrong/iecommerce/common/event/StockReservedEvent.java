package com.chamrong.iecommerce.common.event;

import java.util.List;

public record StockReservedEvent(Long orderId, String tenantId, List<Item> items) {
  public record Item(Long productVariantId, int quantity) {}
}
