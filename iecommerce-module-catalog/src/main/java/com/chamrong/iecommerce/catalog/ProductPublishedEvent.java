package com.chamrong.iecommerce.catalog;

/**
 * Event published when a product transitions to ACTIVE. Consumed by Audit and Inventory modules.
 */
public record ProductPublishedEvent(String tenantId, Long productId) {}
