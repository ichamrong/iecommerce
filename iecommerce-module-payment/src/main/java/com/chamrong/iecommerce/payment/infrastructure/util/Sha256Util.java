package com.chamrong.iecommerce.payment.infrastructure.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility for bank-grade SHA-256 hashing of payloads. Used for webhook deduplication and integrity
 * checks.
 */
public final class Sha256Util {

  private Sha256Util() {}

  /**
   * Calculates the SHA-256 hash of a string payload.
   *
   * @param payload the string to hash
   * @return the hex-encoded hash string
   */
  public static String calculateHash(String payload) {
    if (payload == null) return null;
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not found", e);
    }
  }
}
