package com.chamrong.iecommerce.payment.infrastructure.aba;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.PaymentProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AbaAdapter implements PaymentProviderPort {

  private static final Logger log = LoggerFactory.getLogger(AbaAdapter.class);

  private final String apiKey;
  private final String merchantId;

  public AbaAdapter(
      @Value("${payment.aba.api-key:}") String apiKey,
      @Value("${payment.aba.merchant-id:}") String merchantId) {
    this.apiKey = apiKey;
    this.merchantId = merchantId;
  }

  @Override
  public ProviderResponse createIntent(ProviderRequest request) {
    // ABA PayWay implementation usually requires a hash of parameters
    // This is a simplified version for the hardening phase
    String externalId = "ABA-" + request.intentId();
    String checkoutUrl = "https://checkout.payway.com.kh/pay/" + externalId;

    return new ProviderResponse(
        externalId,
        checkoutUrl,
        null,
        "PENDING",
        null, // No QR for standard PayWay redirect
        null, // No DeepLink for standard PayWay redirect
        null,
        null);
  }

  @Override
  public ProviderResponse capture(String externalId, Money amount) {
    // ABA usually captures automatically or via webhook
    return new ProviderResponse(externalId, null, null, "SUCCEEDED", null, null, null, null);
  }

  @Override
  public ProviderResponse refund(String externalId, Money amount) {
    return new ProviderResponse(
        null,
        null,
        null,
        null,
        null,
        null,
        "not_implemented",
        "ABA refund via API requires specific credentials");
  }

  @Override
  public boolean supports(ProviderType provider) {
    return provider == ProviderType.ABA;
  }
}
