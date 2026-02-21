package com.chamrong.iecommerce.catalog;

/** Event published when a collection is updated. */
public record CollectionUpdatedEvent(String tenantId, Long collectionId) {}
