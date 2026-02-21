package com.chamrong.iecommerce.catalog;

/** Event published when a facet is created. */
public record FacetCreatedEvent(String tenantId, Long facetId, String name) {}
