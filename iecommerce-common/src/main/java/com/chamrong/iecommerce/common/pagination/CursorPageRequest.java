package com.chamrong.iecommerce.common.pagination;

import java.util.Map;
import lombok.Getter;

/**
 * Standard cursor page request: cursor, limit, and optional filters for filterHash binding.
 *
 * <p>Used by list endpoints to parse query params and build the keyset query. Filters should
 * include any query params that affect the result set (e.g. status, search) so that cursors from
 * one filter cannot be used for another.
 */
@Getter
public final class CursorPageRequest {

  private final String cursor;
  private final int limit;

  /** -- GETTER -- Unmodifiable map of filter key to value (for FilterHasher). */
  private final Map<String, Object> filters;

  public CursorPageRequest(String cursor, int limit, Map<String, Object> filters) {
    this.cursor = cursor;
    this.limit = Math.max(1, Math.min(limit, 100)); // clamp 1..100
    this.filters = filters != null ? Map.copyOf(filters) : Map.of();
  }

  public static CursorPageRequest of(String cursor, int limit) {
    return new CursorPageRequest(cursor, limit, null);
  }

  public static CursorPageRequest of(String cursor, int limit, Map<String, Object> filters) {
    return new CursorPageRequest(cursor, limit, filters);
  }

  /** Limit for DB query: request limit + 1 to detect hasNext. */
  public int getLimitPlusOne() {
    return limit + 1;
  }
}
