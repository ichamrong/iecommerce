package com.chamrong.iecommerce.payment.infrastructure.paypal;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.WebhookVerificationPort;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PayPalWebhookVerifier implements WebhookVerificationPort {

  private static final Logger log = LoggerFactory.getLogger(PayPalWebhookVerifier.class);

  // For real production, use PayPal VerifyWebhookSignature API
  // Placeholder for now.

  @Override
  public VerificationResult verify(
      ProviderType provider, String payload, Map<String, String> headers) {
    if (provider != ProviderType.PAYPAL) {
      String hash =
          com.chamrong.iecommerce.payment.infrastructure.util.Sha256Util.calculateHash(payload);
      return new VerificationResult(false, null, null, null, payload, hash);
    }

    // In a real implementation:
    // 1. Get PayPal-Auth-Algo, PayPal-Transmission-Id, etc from headers
    // 2. Call PayPal's verify-webhook-signature endpoint

    log.info("Verifying PayPal webhook (placeholder logic)");

    // Simulate extraction from payload
    String eventId = "pp_evt_" + System.currentTimeMillis();
    String eventType = "orders:completed";
    String intentId = null;

    String hash =
        com.chamrong.iecommerce.payment.infrastructure.util.Sha256Util.calculateHash(payload);
    return new VerificationResult(true, eventId, eventType, intentId, payload, hash);
  }
}
