package com.chamrong.iecommerce.catalog.domain.event;

import java.time.Instant;

/**
 * Domain event when a catalog item (product) is created.
 *
 * <p>Published via outbox or application event publisher for consumers (inventory, search).
 */
public record CatalogItemCreatedEvent(
    String tenantId,
    Long itemId,
    String slug,
    Instant occurredAt) {

  public CatalogItemCreatedEvent(String tenantId, Long itemId, String slug) {
    this(tenantId, itemId, slug, Instant.now());
  }
}
