package com.chamrong.iecommerce.payment.domain.webhook;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import java.util.Map;

/**
 * Outbound port for verifying webhook authenticity from payment providers.
 *
 * <p>Each provider adapter ({@code StripeWebhookVerifier}, {@code PayPalWebhookVerifier}, etc.)
 * implements this interface. Implementations live in {@code infrastructure/provider/}.
 */
public interface WebhookVerificationPort {

  /**
   * Verifies the authenticity of a webhook payload using provider-specific signatures and headers.
   *
   * @param provider the payment provider that sent the webhook
   * @param payload the raw request body
   * @param headers the HTTP headers containing provider signatures
   * @return a {@link VerificationResult} with validity and extracted metadata
   */
  VerificationResult verify(ProviderType provider, String payload, Map<String, String> headers);

  /**
   * Returns true if this verifier handles the given provider. Used by the factory for Strategy
   * selection.
   */
  boolean supports(ProviderType provider);

  /**
   * Immutable result of webhook verification.
   *
   * @param isValid whether the signature is authentic
   * @param providerEventId the provider-assigned event identifier (for deduplication); may be null
   * @param eventType the normalized event type string; may be null
   * @param intentId the associated PaymentIntent ID, if present in the payload; may be null
   * @param rawPayload the raw payload (stored for audit purposes); may be null
   * @param payloadHash SHA-256 hash of rawPayload (for deduplication); may be null
   */
  record VerificationResult(
      boolean isValid,
      String providerEventId, // nullable — use Optional in callers
      String eventType,
      String intentId,
      String rawPayload,
      String payloadHash) {

    public static VerificationResult invalid() {
      return new VerificationResult(false, null, null, null, null, null);
    }
  }
}
