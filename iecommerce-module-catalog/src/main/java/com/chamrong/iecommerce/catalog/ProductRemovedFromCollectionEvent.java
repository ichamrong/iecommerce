package com.chamrong.iecommerce.catalog;

/** Event published when a product is removed from a collection. */
public record ProductRemovedFromCollectionEvent(
    String tenantId, Long collectionId, Long productId) {}
