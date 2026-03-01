package com.chamrong.iecommerce.payment.domain.ports;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.ProviderType;

public interface PaymentProviderPort {

  ProviderResponse createIntent(ProviderRequest request);

  ProviderResponse capture(String externalId, Money amount);

  ProviderResponse refund(String externalId, Money amount);

  boolean supports(ProviderType provider);

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
  }
}
