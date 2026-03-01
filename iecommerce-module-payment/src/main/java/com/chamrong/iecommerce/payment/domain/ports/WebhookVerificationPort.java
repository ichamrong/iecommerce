package com.chamrong.iecommerce.payment.domain.ports;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import java.util.Map;

/** Inbound port for verifying webhook authenticity from various payment providers. */
public interface WebhookVerificationPort {

  /**
   * Verifies the authenticity of a webhook payload using provider-specific signatures and headers.
   *
   * @param provider the payment provider
   * @param payload the raw request body
   * @param headers the HTTP headers containing signatures
   * @return a result containing validity and extracted metadata
   */
  VerificationResult verify(ProviderType provider, String payload, Map<String, String> headers);

  record VerificationResult(
      boolean isValid,
      String providerEventId,
      String eventType,
      String intentId, // Associated PaymentIntent ID if found in payload
      String rawPayload,
      String payloadHash) {}
}
