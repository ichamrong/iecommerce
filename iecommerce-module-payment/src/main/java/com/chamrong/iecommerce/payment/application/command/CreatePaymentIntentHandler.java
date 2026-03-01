package com.chamrong.iecommerce.payment.application.command;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.PaymentIntentRepositoryPort;
import com.chamrong.iecommerce.payment.domain.ports.PaymentProviderPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreatePaymentIntentHandler {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(CreatePaymentIntentHandler.class);

  private final PaymentIntentRepositoryPort repository;
  private final List<PaymentProviderPort> providers;

  public record Command(
      String tenantId,
      Long orderId,
      Money amount,
      ProviderType provider,
      String idempotencyKey,
      String returnUrl,
      String cancelUrl) {}

  /**
   * Handles the creation of a new payment intent.
   *
   * @param cmd the command containing payment details
   * @return the created or existing PaymentIntent
   * @throws IllegalArgumentException if no provider is found
   * @throws RuntimeException if provider communication fails
   */
  @Transactional
  public PaymentIntent handle(Command cmd) {
    // 1. Idempotency Check
    Optional<PaymentIntent> existing =
        repository.findByIdempotencyKey(cmd.tenantId(), cmd.idempotencyKey());
    if (existing.isPresent()) {
      log.info(
          "{} for idempotencyKey={}, returning existing",
          com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.WEBHOOK_DEDUPLICATED,
          cmd.idempotencyKey());
      return existing.get();
    }

    // 2. Create Domain Aggregate
    PaymentIntent intent = createDomainAggregate(cmd);

    // 3. Communicate with Provider
    PaymentProviderPort.ProviderResponse response = callProvider(cmd, intent);

    // 4. Update and Save
    updateAndSaveIntent(intent, response);

    log.info(
        "{} id={} externalId={} provider={}",
        com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.PAYMENT_INTENT_CREATED,
        intent.getIntentId(),
        intent.getExternalId(),
        intent.getProvider());

    return intent;
  }

  private PaymentIntent createDomainAggregate(Command cmd) {
    return new PaymentIntent(
        UUID.randomUUID(),
        cmd.tenantId(),
        cmd.orderId(),
        cmd.amount(),
        cmd.provider(),
        cmd.idempotencyKey());
  }

  private PaymentProviderPort.ProviderResponse callProvider(Command cmd, PaymentIntent intent) {
    PaymentProviderPort providerPort =
        providers.stream()
            .filter(p -> p.supports(cmd.provider()))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("No provider found for: " + cmd.provider()));

    log.debug(
        "{} provider={} intentId={}",
        com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.PROVIDER_CALL_START,
        cmd.provider(),
        intent.getIntentId());

    PaymentProviderPort.ProviderResponse response =
        providerPort.createIntent(
            new PaymentProviderPort.ProviderRequest(
                intent.getIntentId().toString(),
                intent.getTenantId(),
                intent.getAmount(),
                "Order #" + intent.getOrderId(),
                cmd.returnUrl(),
                cmd.cancelUrl()));

    if (!response.isSuccessful()) {
      log.error(
          "{} provider={} error={}",
          com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.PROVIDER_CALL_FAILURE,
          cmd.provider(),
          response.errorMessage());
      throw new RuntimeException(
          "Failed to create intent with provider: " + response.errorMessage());
    }

    return response;
  }

  private void updateAndSaveIntent(
      PaymentIntent intent, PaymentProviderPort.ProviderResponse response) {
    intent.start(
        response.externalId(),
        response.checkoutUrl(),
        response.clientSecret(),
        response.qrCode(),
        response.deepLink());
    repository.save(intent);
  }
}
