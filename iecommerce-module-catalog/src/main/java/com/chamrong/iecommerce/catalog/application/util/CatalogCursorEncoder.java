package com.chamrong.iecommerce.catalog.application.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Encodes and decodes opaque cursor tokens for catalog keyset pagination.
 *
 * <p>Format: {@code Base64(createdAt.epochSecond + "," + id)}. The token is URL-safe (no padding)
 * and hides implementation details from clients.
 *
 * <p>Example round-trip:
 *
 * <pre>
 *   String token = CatalogCursorEncoder.encode(Instant.parse("2024-01-01T00:00:00Z"), 42L);
 *   CatalogCursorDecoded decoded = CatalogCursorEncoder.decode(token);
 *   // decoded.createdAt() == Instant.parse("2024-01-01T00:00:00Z")
 *   // decoded.id() == 42L
 * </pre>
 */
public final class CatalogCursorEncoder {

  private CatalogCursorEncoder() {}

  /**
   * Encodes the cursor for the *last item on the current page*.
   *
   * @param createdAt the item's createdAt timestamp
   * @param id the item's primary key
   * @return URL-safe Base64 token
   */
  public static String encode(Instant createdAt, Long id) {
    String raw = createdAt.getEpochSecond() + "," + id;
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decodes a cursor token into its components.
   *
   * @param token the cursor token from a previous response
   * @return decoded cursor, or {@code null} if the token is null
   * @throws IllegalArgumentException if token is malformed
   */
  public static CatalogCursorDecoded decode(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    try {
      byte[] raw = Base64.getUrlDecoder().decode(token);
      String[] parts = new String(raw, StandardCharsets.UTF_8).split(",", 2);
      if (parts.length != 2) {
        throw new IllegalArgumentException("Malformed catalog cursor token.");
      }
      Instant createdAt = Instant.ofEpochSecond(Long.parseLong(parts[0].trim()));
      Long id = Long.parseLong(parts[1].trim());
      return new CatalogCursorDecoded(createdAt, id);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid catalog cursor token: " + e.getMessage(), e);
    }
  }

  /** Holds the decoded cursor data. */
  public record CatalogCursorDecoded(Instant createdAt, Long id) {}
}
