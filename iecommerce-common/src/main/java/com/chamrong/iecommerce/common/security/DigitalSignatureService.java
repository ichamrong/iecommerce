package com.chamrong.iecommerce.common.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to handle digital signing for important documents like Invoices. In a real-world
 * scenario, this would use RSA/ECDSA Private Keys from a hardware security module (HSM) or a secure
 * KeyStore. For this implementation, we use HMAC-SHA256 as a secure digital footprint.
 */
@Slf4j
@Service
public class DigitalSignatureService {

  @Value("${iecommerce.security.signature.secret:iecommerce-master-secret-key}")
  private String secretKey;

  /** Generates a digital signature for the provided data. */
  public String sign(String data) {
    if (data == null) return null;
    try {
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secret_key =
          new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      sha256_HMAC.init(secret_key);

      byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      log.error("Failed to generate digital signature", e);
      return null;
    }
  }

  /** Verifies if a signature is valid for the given data. */
  public boolean verify(String data, String signature) {
    String expected = sign(data);
    return expected != null && expected.equals(signature);
  }

  /** Masks sensitive data, showing only first and last characters. */
  public String mask(String data) {
    if (data == null || data.length() <= 4) return "****";
    return data.substring(0, 2) + "..." + data.substring(data.length() - 2);
  }

  /** Custom masking with specified visible characters. */
  public String mask(String data, int visible) {
    if (data == null || data.length() <= visible * 2) return "****";
    return data.substring(0, visible) + "***" + data.substring(data.length() - visible);
  }
}
