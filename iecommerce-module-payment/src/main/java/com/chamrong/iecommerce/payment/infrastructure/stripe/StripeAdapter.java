package com.chamrong.iecommerce.payment.infrastructure.stripe;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.PaymentProviderPort;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import java.util.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeAdapter implements PaymentProviderPort {

  private static final Logger log = LoggerFactory.getLogger(StripeAdapter.class);

  private final String webhookSecret;

  public StripeAdapter(
      @Value("${payment.stripe.secret-key}") String secretKey,
      @Value("${payment.stripe.webhook-secret}") String webhookSecret) {
    Stripe.apiKey = secretKey;
    this.webhookSecret = webhookSecret;
  }

  @Override
  public ProviderResponse createIntent(ProviderRequest request) {
    try {
      Currency currency = Currency.getInstance(request.amount().getCurrency());
      PaymentIntentCreateParams params =
          PaymentIntentCreateParams.builder()
              .setAmount(
                  request
                      .amount()
                      .getAmount()
                      .movePointRight(currency.getDefaultFractionDigits())
                      .longValue())
              .setCurrency(currency.getCurrencyCode().toLowerCase())
              .putMetadata("intent_id", request.intentId())
              .putMetadata("tenant_id", request.tenantId())
              .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.AUTOMATIC)
              .build();

      PaymentIntent intent = PaymentIntent.create(params);

      return new ProviderResponse(
          intent.getId(),
          null, // Stripe Elements usually uses client secret
          intent.getClientSecret(),
          intent.getStatus(),
          null,
          null,
          null,
          null);
    } catch (StripeException e) {
      log.error("Stripe intent creation failed", e);
      return new ProviderResponse(null, null, null, null, null, null, e.getCode(), e.getMessage());
    }
  }

  @Override
  public ProviderResponse capture(String externalId, Money amount) {
    try {
      PaymentIntent intent = PaymentIntent.retrieve(externalId);
      intent = intent.capture();
      return new ProviderResponse(
          intent.getId(), null, null, intent.getStatus(), null, null, null, null);
    } catch (StripeException e) {
      log.error("Stripe capture failed", e);
      return new ProviderResponse(null, null, null, null, null, null, e.getCode(), e.getMessage());
    }
  }

  @Override
  public ProviderResponse refund(String externalId, Money amount) {
    try {
      Currency currency = Currency.getInstance(amount.getCurrency());
      RefundCreateParams params =
          RefundCreateParams.builder()
              .setPaymentIntent(externalId)
              .setAmount(
                  amount
                      .getAmount()
                      .movePointRight(currency.getDefaultFractionDigits())
                      .longValue())
              .build();

      Refund refund = Refund.create(params);
      return new ProviderResponse(
          refund.getId(), null, null, refund.getStatus(), null, null, null, null);
    } catch (StripeException e) {
      log.error("Stripe refund failed", e);
      return new ProviderResponse(null, null, null, null, null, null, e.getCode(), e.getMessage());
    }
  }

  @Override
  public boolean supports(ProviderType provider) {
    return provider == ProviderType.STRIPE;
  }
}
