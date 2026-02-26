package com.chamrong.iecommerce.inventory.domain;

/**
 * Saga Event: Fired when an inventory operation (reservation/deduction) fails.
 * Order module listens to this to trigger compensation (e.g., cancel order).
 */
public record InventoryOperationFailedEvent(
    Long orderId,
    String tenantId,
    String reason
) {}
