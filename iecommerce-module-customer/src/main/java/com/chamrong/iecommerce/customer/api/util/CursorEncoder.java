package com.chamrong.iecommerce.customer.api.util;

import java.time.Instant;
import java.util.Base64;

public class CursorEncoder {

  public record Cursor(Instant createdAt, Long id) {}

  public static String encode(Instant createdAt, Long id) {
    if (createdAt == null || id == null) {
      return null;
    }
    String raw = createdAt.toEpochMilli() + "_" + id;
    return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
  }

  public static Cursor decode(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    try {
      String decoded = new String(Base64.getUrlDecoder().decode(cursor));
      String[] parts = decoded.split("_");
      return new Cursor(Instant.ofEpochMilli(Long.parseLong(parts[0])), Long.parseLong(parts[1]));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid cursor format", e);
    }
  }
}
