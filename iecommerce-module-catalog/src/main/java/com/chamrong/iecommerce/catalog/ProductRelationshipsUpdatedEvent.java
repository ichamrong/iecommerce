package com.chamrong.iecommerce.catalog;

/** Event published when product relationships are updated. */
public record ProductRelationshipsUpdatedEvent(String tenantId, Long productId) {}
