package com.chamrong.iecommerce.catalog;

/** Event published when a category is updated. */
public record CategoryUpdatedEvent(String tenantId, Long categoryId, String slug) {}
