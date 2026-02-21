package com.chamrong.iecommerce.catalog;

/** Event published when a collection is created. */
public record CollectionCreatedEvent(String tenantId, Long collectionId, String slug) {}
