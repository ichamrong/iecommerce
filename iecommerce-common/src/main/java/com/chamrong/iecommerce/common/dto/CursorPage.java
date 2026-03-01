package com.chamrong.iecommerce.common.dto;

import java.util.List;
import lombok.Value;

/**
 * Keyset (Cursor) Pagination Response. Hardened for bank-grade stability and cross-module
 * compatibility.
 */
@Value
public class CursorPage<T> {
  List<T> data; // Compatibility with Promotion module
  String nextCursor;
  boolean hasMore; // Compatibility with Promotion module

  public static <T> CursorPage<T> of(List<T> data, String nextCursor, boolean hasMore) {
    return new CursorPage<>(data, nextCursor, hasMore);
  }

  // Alias getters for newer modules if needed, but keeping it simple for now
  public List<T> getContent() {
    return data;
  }

  public boolean isHasNext() {
    return hasMore;
  }
}
