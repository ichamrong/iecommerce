package com.chamrong.iecommerce.common.pagination;

import java.util.List;

/**
 * Standard cursor page response: data, nextCursor, hasNext, limit.
 *
 * <p>Use for all list endpoints. When there is no next page, {@code nextCursor} is null and {@code
 * hasNext} is false.
 */
public final class CursorPageResponse<T> {

  private final List<T> data;
  private final String nextCursor;
  private final boolean hasNext;
  private final int limit;

  public CursorPageResponse(List<T> data, String nextCursor, boolean hasNext, int limit) {
    this.data = data != null ? List.copyOf(data) : List.of();
    this.nextCursor = nextCursor;
    this.hasNext = hasNext;
    this.limit = limit;
  }

  public static <T> CursorPageResponse<T> of(
      List<T> data, String nextCursor, boolean hasNext, int limit) {
    return new CursorPageResponse<>(data, nextCursor, hasNext, limit);
  }

  /** Last page (no next cursor). */
  public static <T> CursorPageResponse<T> lastPage(List<T> data, int limit) {
    return new CursorPageResponse<>(data, null, false, limit);
  }

  /** Page with next cursor. */
  public static <T> CursorPageResponse<T> withNext(List<T> data, String nextCursor, int limit) {
    return new CursorPageResponse<>(data, nextCursor, true, limit);
  }

  public List<T> getData() {
    return data;
  }

  public String getNextCursor() {
    return nextCursor;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public int getLimit() {
    return limit;
  }
}
