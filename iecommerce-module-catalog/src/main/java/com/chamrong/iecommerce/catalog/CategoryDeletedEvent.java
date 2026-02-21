package com.chamrong.iecommerce.catalog;

/** Event published when a category is deleted. */
public record CategoryDeletedEvent(String tenantId, Long categoryId) {}
