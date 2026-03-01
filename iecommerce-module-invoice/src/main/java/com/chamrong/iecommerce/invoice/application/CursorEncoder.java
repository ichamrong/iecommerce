package com.chamrong.iecommerce.invoice.application;

import java.time.Instant;
import java.util.Base64;

/**
 * Encodes cursor values for keyset pagination.
 *
 * <p>Format: Base64Url({issuedAtEpochMilli}:{id}) — opaque to API callers.
 */
final class CursorEncoder {

  private CursorEncoder() {}

  /**
   * Encodes an (issuedAt, id) pair into an opaque cursor string.
   *
   * @param issuedAt timestamp of the last item on the current page
   * @param id primary key of the last item on the current page
   * @return opaque Base64url-encoded cursor
   */
  static String encode(Instant issuedAt, Long id) {
    String raw = (issuedAt != null ? issuedAt.toEpochMilli() : "0") + ":" + id;
    return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
  }
}
