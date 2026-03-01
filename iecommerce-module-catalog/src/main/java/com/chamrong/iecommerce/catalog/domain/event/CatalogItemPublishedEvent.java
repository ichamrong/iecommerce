package com.chamrong.iecommerce.catalog.domain.event;

import java.time.Instant;

/**
 * Domain event when a catalog item is published (DRAFT/ARCHIVED → PUBLISHED).
 *
 * <p>Consumers may invalidate caches or sync to search index.
 */
public record CatalogItemPublishedEvent(
    String tenantId,
    Long itemId,
    String slug,
    Instant occurredAt) {

  public CatalogItemPublishedEvent(String tenantId, Long itemId, String slug) {
    this(tenantId, itemId, slug, Instant.now());
  }
}
