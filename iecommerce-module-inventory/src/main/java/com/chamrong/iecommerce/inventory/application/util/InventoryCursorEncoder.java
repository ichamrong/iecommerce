package com.chamrong.iecommerce.inventory.application.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Encodes and decodes opaque cursor tokens for inventory keyset pagination.
 *
 * <p>Format: {@code Base64url(epochSecond + "," + id)}. URL-safe, no padding.
 *
 * <p>Invariant: {@code decode(encode(t, id)).equals(new CursorDecoded(t, id))} for any valid t, id.
 */
public final class InventoryCursorEncoder {

  private InventoryCursorEncoder() {}

  /**
   * Encodes the cursor for the last item on the current page.
   *
   * @param createdAt item's createdAt timestamp
   * @param id item's primary key
   * @return URL-safe Base64 token
   */
  public static String encode(Instant createdAt, Long id) {
    String raw = createdAt.getEpochSecond() + "," + id;
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decodes an opaque cursor token.
   *
   * @param token the token from a previous response; null or blank returns null (first-page signal)
   * @return decoded cursor, or null if token is null/blank
   * @throws IllegalArgumentException if token is not valid Base64 or has wrong format
   */
  public static CursorDecoded decode(String token) {
    if (token == null || token.isBlank()) return null;
    try {
      byte[] bytes = Base64.getUrlDecoder().decode(token);
      String raw = new String(bytes, StandardCharsets.UTF_8);
      String[] parts = raw.split(",", 2);
      if (parts.length != 2) {
        throw new IllegalArgumentException("Malformed inventory cursor token: " + token);
      }
      return new CursorDecoded(
          Instant.ofEpochSecond(Long.parseLong(parts[0])), Long.parseLong(parts[1]));
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Malformed inventory cursor token: " + token, e);
    }
  }

  /** Decoded cursor values. */
  public record CursorDecoded(Instant createdAt, Long id) {}
}
