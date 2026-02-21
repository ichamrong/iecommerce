package com.chamrong.iecommerce.catalog;

/** Event published when a product is deleted (soft-deleted). */
public record ProductDeletedEvent(String tenantId, Long productId) {}
