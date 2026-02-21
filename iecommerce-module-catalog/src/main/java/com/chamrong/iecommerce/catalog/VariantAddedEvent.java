package com.chamrong.iecommerce.catalog;

/** Event published when a product variant is added. */
public record VariantAddedEvent(String tenantId, Long productId, String sku) {}
