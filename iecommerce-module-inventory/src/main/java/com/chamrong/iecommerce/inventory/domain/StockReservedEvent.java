package com.chamrong.iecommerce.inventory.domain;

import java.util.List;

/**
 * Saga Event: Fired when stock has been successfully reserved for an order.
 * Payment module listens to this to initiate the payment process.
 */
public record StockReservedEvent(
    Long orderId,
    String tenantId,
    List<Item> items
) {
    public record Item(Long productVariantId, int quantity) {}
}
