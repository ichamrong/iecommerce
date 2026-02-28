package com.chamrong.iecommerce.inventory.application.dto;

import java.util.List;

/**
 * Generic cursor-paginated response wrapper for all inventory list endpoints.
 *
 * <p>Same contract as catalog's {@code CatalogCursorResponse} — kept separate to avoid cross-module
 * type coupling.
 *
 * @param <T> item type
 * @param data current page items (up to {@code limit} elements)
 * @param nextCursor opaque cursor to use as {@code ?cursor=} on the next request; null on last page
 * @param hasNext {@code true} if there are more items beyond this page
 */
public record InventoryCursorResponse<T>(List<T> data, String nextCursor, boolean hasNext) {}
