package com.chamrong.iecommerce.order.application.dto;

import java.util.List;

/**
 * Generic cursor-paginated response envelope for all order listing endpoints.
 *
 * <p>Cursor is an opaque Base64-URL token — clients must not parse or construct it. Pass {@code
 * nextCursor} as the {@code ?cursor=} parameter on the next request. When {@code hasNext} is false,
 * no further pages exist.
 *
 * @param <T> item type (e.g. {@link OrderSummaryResponse}, {@link AuditLogResponse})
 */
public record OrderCursorResponse<T>(List<T> data, String nextCursor, boolean hasNext) {

  /** Convenience factory for a non-empty page with more data beyond. */
  public static <T> OrderCursorResponse<T> of(List<T> data, String nextCursor) {
    return new OrderCursorResponse<>(data, nextCursor, true);
  }

  /** Convenience factory for the last page. */
  public static <T> OrderCursorResponse<T> last(List<T> data) {
    return new OrderCursorResponse<>(data, null, false);
  }
}
