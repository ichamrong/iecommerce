package com.chamrong.iecommerce.payment.infrastructure.paypal;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.PaymentProviderPort;
import com.paypal.sdk.models.*;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PayPalAdapter implements PaymentProviderPort {

  private static final Logger log = LoggerFactory.getLogger(PayPalAdapter.class);

  // Logic to initialize Client would go here
  // private final PaypalServerSdkClient client;

  @Override
  public ProviderResponse createIntent(ProviderRequest request) {
    // PayPal Server SDK 2.x uses Models from com.paypal.sdk.models

    AmountWithBreakdown amount =
        new AmountWithBreakdown.Builder()
            .currencyCode(request.amount().getCurrency())
            .value(request.amount().getAmount().toString())
            .build();

    PurchaseUnitRequest purchaseUnit =
        new PurchaseUnitRequest.Builder()
            .amount(amount)
            .referenceId(request.intentId())
            .description(request.description())
            .build();

    OrderRequest orderRequest =
        new OrderRequest.Builder()
            .intent(CheckoutPaymentIntent.CAPTURE)
            .purchaseUnits(Collections.singletonList(purchaseUnit))
            .build();

    // Mocking response structure for now as per SDK 2.x
    return new ProviderResponse(
        "PAYPAL_ORDER_ID",
        "https://www.paypal.com/checkoutnow?token=PAYPAL_ORDER_ID",
        null,
        "CREATED",
        null,
        null,
        null,
        null);
  }

  @Override
  public ProviderResponse capture(String externalId, Money amount) {
    return new ProviderResponse(externalId, null, null, "COMPLETED", null, null, null, null);
  }

  @Override
  public ProviderResponse refund(String externalId, Money amount) {
    return new ProviderResponse(
        null, null, null, null, null, null, "not_implemented", "PayPal refund not yet implemented");
  }

  @Override
  public boolean supports(ProviderType provider) {
    return provider == ProviderType.PAYPAL;
  }
}
