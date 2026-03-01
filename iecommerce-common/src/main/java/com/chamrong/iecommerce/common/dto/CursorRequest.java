package com.chamrong.iecommerce.common.dto;

import lombok.Value;

/** Keyset (Cursor) Pagination Request. */
@Value
public class CursorRequest {
  String cursor;
  int limit;
}
