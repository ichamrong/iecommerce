package com.chamrong.iecommerce.common.pagination;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Computes a stable SHA-256 hex hash of filter parameters for cursor integrity.
 *
 * <p>Canonical form: keys sorted; only non-null values; endpoint/module name included to prevent
 * cross-endpoint cursor reuse.
 */
public final class FilterHasher {

  private static final String SHA256 = "SHA-256";

  private FilterHasher() {}

  /**
   * Computes SHA-256 hex hash of a canonical representation of the filters.
   *
   * @param endpointOrModuleName included in hash to bind cursor to this list endpoint
   * @param filters map of filter key to value (only non-null values are included; keys sorted)
   * @return lowercase hex string, or "" if no filters
   */
  public static String computeHash(String endpointOrModuleName, Map<String, Object> filters) {
    TreeMap<String, Object> canonical = new TreeMap<>();
    if (endpointOrModuleName != null && !endpointOrModuleName.isEmpty()) {
      canonical.put("_endpoint", endpointOrModuleName);
    }
    if (filters != null) {
      filters.entrySet().stream()
          .filter(e -> e.getValue() != null)
          .forEach(e -> canonical.put(e.getKey(), e.getValue()));
    }
    if (canonical.isEmpty()) {
      return "";
    }
    String canonicalJson = toCanonicalJson(canonical);
    return sha256Hex(canonicalJson);
  }

  /** Builds a stable string representation: keys in order, simple value serialization. */
  private static String toCanonicalJson(TreeMap<String, Object> map) {
    if (map.isEmpty()) return "{}";
    String pairs =
        map.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> "\"" + escape(e.getKey()) + "\":" + toJsonValue(e.getValue()))
            .collect(Collectors.joining(","));
    return "{" + pairs + "}";
  }

  private static String toJsonValue(Object v) {
    if (v == null) return "null";
    if (v instanceof String) return "\"" + escape((String) v) + "\"";
    if (v instanceof Number || v instanceof Boolean) return v.toString();
    if (v instanceof java.time.Instant) return "\"" + v.toString() + "\"";
    return "\"" + escape(v.toString()) + "\"";
  }

  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static String sha256Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance(SHA256);
      byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
