package com.chamrong.iecommerce.payment.domain.paymentintent;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.ProviderType;

/**
 * Outbound port for payment provider operations (Strategy pattern).
 *
 * <p>Each payment provider (Stripe, PayPal, ABA, Bakong) implements this interface. The concrete
 * adapters live in {@code infrastructure/provider/}.
 */
public interface PaymentProviderPort {

  /**
   * Creates a payment intent with the provider and returns checkout data.
   *
   * @param request the provider-agnostic request
   * @return the provider response with checkout URL and/or client secret
   */
  ProviderResponse createIntent(ProviderRequest request);

  /**
   * Captures a previously authorized payment.
   *
   * @param externalId the provider-assigned intent ID
   * @param amount the amount to capture
   * @return the capture result
   */
  ProviderResponse capture(String externalId, Money amount);

  /**
   * Refunds a payment.
   *
   * @param externalId the provider-assigned reference
   * @param amount the amount to refund
   * @return the refund result
   */
  ProviderResponse refund(String externalId, Money amount);

  /**
   * Returns true if this adapter handles the given provider type. Used by {@code
   * PaymentProviderFactory} for Strategy selection.
   */
  boolean supports(ProviderType provider);

  /** Returns the provider type this adapter handles. Used for registration in the factory map. */
  ProviderType supportedType();

  // ── Nested request/response value objects ─────────────────────────────────

  record ProviderRequest(
      String intentId,
      String tenantId,
      Money amount,
      String description,
      String returnUrl,
      String cancelUrl) {}

  record ProviderResponse(
      String externalId,
      String checkoutUrl,
      String clientSecret,
      String status,
      String qrCode,
      String deepLink,
      String errorCode,
      String errorMessage) {

    public boolean isSuccessful() {
      return errorCode == null;
    }

    public static ProviderResponse failure(String code, String message) {
      return new ProviderResponse(null, null, null, null, null, null, code, message);
    }
  }
}
