package com.chamrong.iecommerce.staff.application.util;

import com.chamrong.iecommerce.staff.infrastructure.persistence.StaffCursorDecoded;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Encodes/decodes opaque cursor tokens for staff listing pagination.
 *
 * <p>Cursor format (before Base64): {@code <epochMillis>_<id>}
 *
 * <p>Example: {@code 1735000000000_42} → Base64 → {@code "MTczNTAwMDAwMDAwMF80Mg=="}
 */
public final class StaffCursorEncoder {

  private StaffCursorEncoder() {}

  public static String encode(Instant createdAt, Long id) {
    String raw = createdAt.toEpochMilli() + "_" + id;
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decodes cursor. Returns {@code null} for null/empty input (first page).
   *
   * @throws IllegalArgumentException if cursor is malformed (tampered)
   */
  public static StaffCursorDecoded decode(String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    try {
      String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
      String[] parts = raw.split("_", 2);
      Instant createdAt = Instant.ofEpochMilli(Long.parseLong(parts[0]));
      Long id = Long.parseLong(parts[1]);
      return new StaffCursorDecoded(createdAt, id);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid cursor token", e);
    }
  }
}
