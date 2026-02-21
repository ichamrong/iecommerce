package com.chamrong.iecommerce.catalog;

/** Event published when a facet value is added. */
public record FacetValueAddedEvent(String tenantId, Long facetId, String code) {}
