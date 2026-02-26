package com.chamrong.iecommerce.common.event;

public record InventoryOperationFailedEvent(Long orderId, String tenantId, String reason) {}
