package com.chamrong.iecommerce.catalog;

/** Event published when a facet value is removed. */
public record FacetValueRemovedEvent(String tenantId, Long facetId, String code) {}
