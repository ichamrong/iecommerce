package com.chamrong.iecommerce.invoice.application;

import java.time.Instant;
import java.util.Base64;

/**
 * Decodes opaque cursor tokens produced by {@link CursorEncoder}.
 *
 * @param issuedAt the decoded timestamp
 * @param id the decoded primary key
 */
record CursorDecoder(Instant issuedAt, Long id) {

  /**
   * Parses a cursor string produced by {@link CursorEncoder#encode}.
   *
   * @param cursor Base64url-encoded cursor string
   * @return decoded cursor, or a zero-value cursor if decoding fails
   */
  static CursorDecoder decode(String cursor) {
    try {
      String raw = new String(Base64.getUrlDecoder().decode(cursor));
      String[] parts = raw.split(":", 2);
      Instant issuedAt =
          "0".equals(parts[0]) ? null : Instant.ofEpochMilli(Long.parseLong(parts[0]));
      Long id = Long.parseLong(parts[1]);
      return new CursorDecoder(issuedAt, id);
    } catch (Exception e) {
      // Invalid cursor treated as first page (safe-defaults)
      return new CursorDecoder(null, null);
    }
  }
}
