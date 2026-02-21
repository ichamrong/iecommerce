package com.chamrong.iecommerce.catalog;

/** Event published when a new product is created. Consumed by Audit. */
public record ProductCreatedEvent(String tenantId, Long productId) {}
