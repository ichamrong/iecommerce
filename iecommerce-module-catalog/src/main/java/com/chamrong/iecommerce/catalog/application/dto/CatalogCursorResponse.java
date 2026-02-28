package com.chamrong.iecommerce.catalog.application.dto;

import java.util.List;

/**
 * Generic cursor-paginated response wrapper for all catalog list endpoints.
 *
 * @param <T> the item type
 * @param data the current page of items (up to {@code limit} elements)
 * @param nextCursor opaque cursor to pass as {@code ?cursor=} on the next request; {@code null} on
 *     the last page
 * @param hasNext {@code true} if there are more items beyond this page
 */
public record CatalogCursorResponse<T>(List<T> data, String nextCursor, boolean hasNext) {}
