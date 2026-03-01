package com.chamrong.iecommerce.payment.infrastructure.stripe;

import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.WebhookVerificationPort;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeWebhookVerifier implements WebhookVerificationPort {

  private static final Logger log = LoggerFactory.getLogger(StripeWebhookVerifier.class);

  private final String webhookSecret;

  public StripeWebhookVerifier(@Value("${payment.stripe.webhook-secret}") String webhookSecret) {
    this.webhookSecret = webhookSecret;
  }

  @Override
  public VerificationResult verify(
      ProviderType provider, String payload, Map<String, String> headers) {
    if (provider != ProviderType.STRIPE) {
      return new VerificationResult(false, null, null, null, payload, null);
    }

    String sigHeader = headers.get("stripe-signature");
    if (sigHeader == null) {
      return new VerificationResult(false, null, null, null, payload, null);
    }

    try {
      Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

      String intentId = null;
      if (event.getDataObjectDeserializer().getObject().isPresent()) {
        Object stripeObj = event.getDataObjectDeserializer().getObject().get();
        if (stripeObj instanceof PaymentIntent pi) {
          intentId = pi.getMetadata().get("intent_id");
        }
      }

      String hash =
          com.chamrong.iecommerce.payment.infrastructure.util.Sha256Util.calculateHash(payload);
      return new VerificationResult(true, event.getId(), event.getType(), intentId, payload, hash);
    } catch (SignatureVerificationException e) {
      log.warn("Stripe webhook signature verification failed", e);
      String hash =
          com.chamrong.iecommerce.payment.infrastructure.util.Sha256Util.calculateHash(payload);
      return new VerificationResult(false, null, null, null, payload, hash);
    }
  }
}
