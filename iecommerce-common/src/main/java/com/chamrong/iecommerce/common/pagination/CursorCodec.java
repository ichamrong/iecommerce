package com.chamrong.iecommerce.common.pagination;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Encodes and decodes cursor payloads as Base64URL (no padding).
 *
 * <p>Format: Base64URL(JSON) with keys {@code v}, {@code createdAt}, {@code id}, {@code
 * filterHash}. On decode, throws {@link InvalidCursorException} with stable error codes for
 * malformed or unsupported cursors.
 */
public final class CursorCodec {

  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  private CursorCodec() {}

  /**
   * Encodes a payload to an opaque Base64URL string.
   *
   * @param payload the cursor payload (non-null)
   * @return Base64URL string, no padding
   */
  public static String encode(CursorPayload payload) {
    if (payload == null) {
      throw new IllegalArgumentException("payload must not be null");
    }
    String json = toJson(payload);
    return ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decodes a cursor string to a payload. Validates version.
   *
   * @param cursor the opaque cursor (may be null or blank for first page)
   * @return decoded payload
   * @throws InvalidCursorException if cursor is malformed or version unsupported
   */
  public static CursorPayload decode(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      throw new InvalidCursorException(
          InvalidCursorException.INVALID_CURSOR, "Cursor is null or blank");
    }
    try {
      String json = new String(DECODER.decode(cursor), StandardCharsets.UTF_8);
      CursorPayload payload = fromJson(json);
      payload.validateVersion();
      return payload;
    } catch (IllegalArgumentException e) {
      throw new InvalidCursorException(
          InvalidCursorException.INVALID_CURSOR, "Invalid Base64 or JSON: " + e.getMessage(), e);
    }
  }

  /**
   * Decodes and optionally validates filter hash. If {@code expectedFilterHash} is non-null and
   * non-empty, cursor's filterHash must equal it.
   *
   * @param cursor opaque cursor
   * @param expectedFilterHash hash computed from current request filters, or null to skip check
   * @return decoded payload
   * @throws InvalidCursorException if cursor invalid or filter hash mismatch
   */
  public static CursorPayload decodeAndValidateFilter(String cursor, String expectedFilterHash) {
    CursorPayload payload = decode(cursor);
    if (expectedFilterHash != null && !expectedFilterHash.isEmpty()) {
      String actual = payload.getFilterHash() != null ? payload.getFilterHash() : "";
      if (!expectedFilterHash.equals(actual)) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR_FILTER_MISMATCH,
            "Cursor was produced with different filters");
      }
    }
    return payload;
  }

  // ── Minimal JSON (no external dependency) ────────────────────────────────────────────────

  private static String toJson(CursorPayload p) {
    String createdAtStr = p.getCreatedAt() != null ? p.getCreatedAt().toString() : "";
    return "{"
        + "\"v\":"
        + p.getV()
        + ",\"createdAt\":\""
        + escape(createdAtStr)
        + "\""
        + ",\"id\":\""
        + escape(p.getId())
        + "\""
        + ",\"filterHash\":\""
        + escape(p.getFilterHash() != null ? p.getFilterHash() : "")
        + "\""
        + "}";
  }

  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static CursorPayload fromJson(String json) {
    if (json == null || json.isBlank()) {
      throw new InvalidCursorException(InvalidCursorException.INVALID_CURSOR, "Empty JSON");
    }
    int v = extractInt(json, "v");
    String createdAtStr = extractString(json, "createdAt");
    String id = extractString(json, "id");
    String filterHash = extractString(json, "filterHash");
    Instant createdAt;
    try {
      createdAt =
          createdAtStr != null && !createdAtStr.isEmpty()
              ? Instant.parse(createdAtStr)
              : Instant.EPOCH;
    } catch (Exception e) {
      throw new InvalidCursorException(
          InvalidCursorException.INVALID_CURSOR, "Invalid createdAt: " + createdAtStr, e);
    }
    return new CursorPayload(v, createdAt, id != null ? id : "", filterHash);
  }

  private static int extractInt(String json, String key) {
    String q = "\"" + key + "\":";
    int start = json.indexOf(q);
    if (start == -1) return 1;
    start += q.length();
    int end = start;
    while (end < json.length()
        && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
      end++;
    }
    try {
      return Integer.parseInt(json.substring(start, end).trim());
    } catch (NumberFormatException e) {
      return 1;
    }
  }

  private static String extractString(String json, String key) {
    String q = "\"" + key + "\":\"";
    int start = json.indexOf(q);
    if (start == -1) return "";
    start += q.length();
    int end = start;
    while (end < json.length()) {
      char c = json.charAt(end);
      if (c == '\\' && end + 1 < json.length()) {
        end += 2;
        continue;
      }
      if (c == '"') break;
      end++;
    }
    String raw = json.substring(start, end);
    return unescape(raw);
  }

  private static String unescape(String s) {
    if (s == null) return "";
    return s.replace("\\\"", "\"").replace("\\\\", "\\");
  }
}
