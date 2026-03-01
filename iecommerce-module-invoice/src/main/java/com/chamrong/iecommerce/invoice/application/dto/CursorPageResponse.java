package com.chamrong.iecommerce.invoice.application.dto;

import java.util.List;

/**
 * Generic cursor-paginated response envelope.
 *
 * @param <T> item type
 * @param data items on this page
 * @param nextCursor opaque cursor for the next page; null if no more pages
 * @param hasNext true if another page exists
 */
public record CursorPageResponse<T>(List<T> data, String nextCursor, boolean hasNext) {

  /** Convenience factory for the last page (no next cursor). */
  public static <T> CursorPageResponse<T> lastPage(List<T> data) {
    return new CursorPageResponse<>(data, null, false);
  }

  /** Convenience factory when more pages exist. */
  public static <T> CursorPageResponse<T> withNext(List<T> data, String nextCursor) {
    return new CursorPageResponse<>(data, nextCursor, true);
  }
}
