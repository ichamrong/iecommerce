package com.chamrong.iecommerce.payment.infrastructure.aba;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.WebhookVerificationPort;
import com.chamrong.iecommerce.payment.infrastructure.util.Sha256Util;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Verifier for ABA Bank webhooks. Uses HMAC-SHA512 for signature verification as per ABA API
 * standards.
 */
@Component
public class ABAWebhookVerifier implements WebhookVerificationPort {

  private static final Logger log = LoggerFactory.getLogger(ABAWebhookVerifier.class);

  private final ABAConfiguration config;

  public ABAWebhookVerifier(ABAConfiguration config) {
    this.config = config;
  }

  @Override
  public VerificationResult verify(
      ProviderType provider, String payload, Map<String, String> headers) {
    if (provider != ProviderType.ABA) {
      return new VerificationResult(
          false, null, null, null, payload, Sha256Util.calculateHash(payload));
    }

    // ABA usually sends the hash in the payload or a specific header
    // For this implementation, we assume a header 'aba-hash' or similar
    String receivedHash = headers.get("x-aba-hash");
    if (receivedHash == null) {
      log.warn("Missing ABA hash header");
      return new VerificationResult(
          false, null, null, null, payload, Sha256Util.calculateHash(payload));
    }

    // In production, ABA webhooks contain fields that must be concatenated in a specific order
    // To simplify, we verify the raw payload if that's what's hashed,
    // or parse the JSON to get the fields.
    boolean isValid = verifyHmac(payload, receivedHash, config.getApiKey());

    // Extract basic info from payload (simplified)
    // Assuming JSON like: {"tran_id": "...", "status": "...", "intent_id": "..."}
    String eventId =
        "aba_" + System.currentTimeMillis(); // Placeholder for real event ID extraction
    String eventType = "PAID"; // Simplified
    String intentId = null; // Extract from payload metadata if available

    return new VerificationResult(
        isValid, eventId, eventType, intentId, payload, Sha256Util.calculateHash(payload));
  }

  private boolean verifyHmac(String data, String expectedHash, String key) {
    try {
      Mac sha512Hmac = Mac.getInstance("HmacSHA512");
      SecretKeySpec secretKey =
          new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
      sha512Hmac.init(secretKey);
      byte[] hash = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      String calculatedHash = Base64.getEncoder().encodeToString(hash);
      return calculatedHash.equals(expectedHash);
    } catch (Exception e) {
      log.error("ABA HMAC verification failed", e);
      return false;
    }
  }
}
