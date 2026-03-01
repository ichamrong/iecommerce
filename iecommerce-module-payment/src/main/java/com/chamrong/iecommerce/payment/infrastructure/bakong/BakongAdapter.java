package com.chamrong.iecommerce.payment.infrastructure.bakong;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.PaymentProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BakongAdapter implements PaymentProviderPort {

  private static final Logger log = LoggerFactory.getLogger(BakongAdapter.class);

  private final String token;
  private final String merchantId;

  public BakongAdapter(
      @Value("${payment.bakong.token:}") String token,
      @Value("${payment.bakong.merchant-id:}") String merchantId) {
    this.token = token;
    this.merchantId = merchantId;
  }

  @Override
  public ProviderResponse createIntent(ProviderRequest request) {
    // Bakong implementation returns a KHQR string or a deep link
    String externalId = "BK-" + request.intentId();
    String qrCode = "000201010212..."; // Simplified KHQR string
    String deepLink = "bakong://pay?id=" + externalId;

    return new ProviderResponse(externalId, null, null, "PENDING", qrCode, deepLink, null, null);
  }

  @Override
  public ProviderResponse capture(String externalId, Money amount) {
    // Bakong is primarily push-based with callbacks
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
        "Bakong refund requires manual transfer or specific API");
  }

  @Override
  public boolean supports(ProviderType provider) {
    return provider == ProviderType.BAKONG;
  }
}
