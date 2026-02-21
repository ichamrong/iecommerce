package com.chamrong.iecommerce.catalog;

/** Event published when a product variant is updated. */
public record VariantUpdatedEvent(String tenantId, Long productId, Long variantId) {}
