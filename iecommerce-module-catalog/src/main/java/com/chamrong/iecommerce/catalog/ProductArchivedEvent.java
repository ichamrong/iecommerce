package com.chamrong.iecommerce.catalog;

/**
 * Event published when a product transitions to ARCHIVED. Consumed by Audit and Inventory modules.
 */
public record ProductArchivedEvent(String tenantId, Long productId) {}
