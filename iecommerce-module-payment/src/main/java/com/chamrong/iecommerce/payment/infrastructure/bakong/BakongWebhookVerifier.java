package com.chamrong.iecommerce.payment.infrastructure.bakong;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.WebhookVerificationPort;
import com.chamrong.iecommerce.payment.infrastructure.util.Sha256Util;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Verifier for Bakong (KHQR) webhooks. In production, this uses the Bakong check-transaction API or
 * signature verification.
 */
@Component
public class BakongWebhookVerifier implements WebhookVerificationPort {

  private static final Logger log = LoggerFactory.getLogger(BakongWebhookVerifier.class);

  private final BakongConfiguration config;

  public BakongWebhookVerifier(BakongConfiguration config) {
    this.config = config;
  }

  @Override
  public VerificationResult verify(
      ProviderType provider, String payload, Map<String, String> headers) {
    if (provider != ProviderType.BAKONG) {
      return new VerificationResult(
          false, null, null, null, payload, Sha256Util.calculateHash(payload));
    }

    String signature = headers.get("x-bakong-signature");
    // Placeholder logic for Bakong signature verification
    // In real scenarios, this might involve RSA or HMAC depending on the specific integration
    boolean isValid = (signature != null);

    log.info("Verifying Bakong webhook (signature present: {})", isValid);

    String eventId = "bakong_" + System.currentTimeMillis();
    String eventType = "SUCCESS";
    String intentId = null;

    return new VerificationResult(
        isValid, eventId, eventType, intentId, payload, Sha256Util.calculateHash(payload));
  }
}
