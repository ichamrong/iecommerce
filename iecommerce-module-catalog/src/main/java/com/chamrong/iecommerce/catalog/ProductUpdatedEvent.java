package com.chamrong.iecommerce.catalog;

/** Event published when a product is updated. */
public record ProductUpdatedEvent(String tenantId, Long productId) {}
