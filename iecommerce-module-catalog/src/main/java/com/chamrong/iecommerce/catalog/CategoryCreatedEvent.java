package com.chamrong.iecommerce.catalog;

/** Event published when a category is created. */
public record CategoryCreatedEvent(String tenantId, Long categoryId, String slug) {}
