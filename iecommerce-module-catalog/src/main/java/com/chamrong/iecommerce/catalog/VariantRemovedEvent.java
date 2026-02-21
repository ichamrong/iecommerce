package com.chamrong.iecommerce.catalog;

/** Event published when a product variant is removed. */
public record VariantRemovedEvent(String tenantId, Long productId, Long variantId) {}
