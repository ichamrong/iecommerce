package com.chamrong.iecommerce.order.application.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Encodes and decodes opaque keyset cursor tokens for cursor-paginated order queries.
 *
 * <p>Format (before Base64-URL encoding): {@code "<epochSecond>,<id>"}. <br>
 * Example decoded: {@code "1709123456,42"} → {@code Instant.ofEpochSecond(1709123456), id=42}.
 *
 * <p>Design rationale — keyset over offset:
 *
 * <ul>
 *   <li>Offset pagination does {@code LIMIT n OFFSET m} — full table scan up to offset m.
 *   <li>Keyset pagination uses a composite WHERE clause that hits the index leaf directly: {@code
 *       WHERE (created_at, id) < (cursorTs, cursorId)} → O(log n) not O(n).
 *   <li>No drift under concurrent inserts (rows inserted between pages don't shift offsets).
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderCursorEncoder {

  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  /** Encodes a (timestamp, id) pair to an opaque cursor string. */
  public static String encode(Instant ts, Long id) {
    String raw = ts.getEpochSecond() + "," + id;
    return ENCODER.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decodes a cursor string back to (timestamp, id).
   *
   * @return decoded cursor, or {@code null} if {@code token} is null or blank
   * @throws IllegalArgumentException if the token is non-null/non-blank but malformed
   */
  public static Decoded decode(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    try {
      String raw = new String(DECODER.decode(token), StandardCharsets.UTF_8);
      String[] parts = raw.split(",", 2);
      if (parts.length != 2) {
        throw new IllegalArgumentException("Malformed cursor token: " + token);
      }
      Instant ts = Instant.ofEpochSecond(Long.parseLong(parts[0]));
      Long id = Long.parseLong(parts[1]);
      return new Decoded(ts, id);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Malformed cursor token: " + token, e);
    }
  }

  /** Value type returned by {@link #decode(String)}. */
  public record Decoded(Instant ts, Long id) {}
}
