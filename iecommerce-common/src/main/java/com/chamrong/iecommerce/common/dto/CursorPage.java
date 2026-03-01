package com.chamrong.iecommerce.common.dto;

import java.util.List;
import lombok.Value;

/** Generic response for cursor-based pagination. */
@Value
public class CursorPage<T> {
  List<T> data;
  String nextCursor;
  boolean hasMore;

  public static <T> CursorPage<T> of(List<T> data, String nextCursor, boolean hasMore) {
    return new CursorPage<>(data, nextCursor, hasMore);
  }
}
