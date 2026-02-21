package com.chamrong.iecommerce.catalog;

/** Event published when a product is added to a collection. */
public record ProductAddedToCollectionEvent(String tenantId, Long collectionId, Long productId) {}
