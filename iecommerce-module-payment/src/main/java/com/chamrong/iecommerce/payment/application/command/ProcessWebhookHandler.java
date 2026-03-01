package com.chamrong.iecommerce.payment.application.command;

import com.chamrong.iecommerce.payment.domain.FinancialLedgerEntry;
import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import com.chamrong.iecommerce.payment.domain.PaymentStatus;
import com.chamrong.iecommerce.payment.domain.PaymentTransaction;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.FinancialLedgerPort;
import com.chamrong.iecommerce.payment.domain.ports.PaymentIntentRepositoryPort;
import com.chamrong.iecommerce.payment.domain.ports.WebhookVerificationPort.VerificationResult;
import com.chamrong.iecommerce.payment.domain.webhook.WebhookDeduplicationPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProcessWebhookHandler {

  private static final Logger log = LoggerFactory.getLogger(ProcessWebhookHandler.class);

  private final PaymentIntentRepositoryPort intentRepository;
  private final WebhookDeduplicationPort dedupPort;
  private final FinancialLedgerPort ledgerPort;
  private final MeterRegistry meterRegistry;

  private Counter webhookCounter;
  private Counter deduplicatedCounter;

  @jakarta.annotation.PostConstruct
  public void init() {
    this.webhookCounter = meterRegistry.counter("payment.webhook.processed");
    this.deduplicatedCounter = meterRegistry.counter("payment.webhook.deduplicated");
  }

  /**
   * Processes an incoming webhook event from a payment provider.
   *
   * @param provider the provider of the webhook
   * @param result the result of the webhook verification
   */
  @Transactional
  public void handle(ProviderType provider, VerificationResult result) {
    log.debug(
        "{} provider={} eventId={} type={}",
        com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.WEBHOOK_RECEIVED,
        provider,
        result.providerEventId(),
        result.eventType());

    // 1. Deduplication
    if (isProcessed(provider, result)) {
      return;
    }

    // 2. Find Payment Intent
    PaymentIntent intent = findIntent(provider, result);
    if (intent == null) {
      return;
    }

    // 3. Process Transaction
    processTransaction(provider, result, intent);

    // 4. Update and Save
    finalizeWebhook(provider, result, intent);
  }

  private boolean isProcessed(ProviderType provider, VerificationResult result) {
    if (dedupPort.isAlreadyProcessed(result.providerEventId())) {
      log.info(
          "{} provider={} eventId={}",
          com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.WEBHOOK_DEDUPLICATED,
          provider,
          result.providerEventId());
      deduplicatedCounter.increment();
      return true;
    }
    return false;
  }

  private PaymentIntent findIntent(ProviderType provider, VerificationResult result) {
    if (result.intentId() == null) {
      log.warn(
          "{} Missing intentId for provider={} eventId={}",
          com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents
              .WEBHOOK_VERIFICATION_FAILED,
          provider,
          result.providerEventId());
      return null;
    }

    return intentRepository
        .findById(UUID.fromString(result.intentId()))
        .orElseGet(
            () -> {
              log.warn(
                  "{} intentId={} not found for provider={} eventId={}",
                  com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents
                      .WEBHOOK_VERIFICATION_FAILED,
                  result.intentId(),
                  provider,
                  result.providerEventId());
              return null;
            });
  }

  private void processTransaction(
      ProviderType provider, VerificationResult result, PaymentIntent intent) {
    PaymentTransaction.TransactionType txType = mapToTransactionType(result.eventType());
    PaymentTransaction tx =
        new PaymentTransaction(
            null, result.providerEventId(), txType, intent.getAmount(), result.eventType());

    intent.recordTransaction(tx);

    if (intent.getStatus() == PaymentStatus.SUCCEEDED) {
      recordLedger(provider, result, intent);
    } else if (isFailed(result.eventType())) {
      intent.fail("webhook_failure", "Failed via " + provider + " event: " + result.eventType());
    }
  }

  private void recordLedger(
      ProviderType provider, VerificationResult result, PaymentIntent intent) {
    FinancialLedgerEntry entry =
        new FinancialLedgerEntry(
            UUID.randomUUID(),
            intent.getTenantId(),
            intent.getOrderId(),
            intent.getIntentId(),
            intent.getAmount(),
            FinancialLedgerEntry.LedgerType.CREDIT,
            "Payment received via " + provider + " (Event: " + result.providerEventId() + ")");
    ledgerPort.record(entry);
    log.info(
        "{} intentId={} provider={} eventId={}",
        com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.LEDGER_POSTED,
        intent.getIntentId(),
        provider,
        result.providerEventId());
  }

  private void finalizeWebhook(
      ProviderType provider, VerificationResult result, PaymentIntent intent) {
    intentRepository.save(intent);
    dedupPort.markAsProcessed(result.providerEventId());
    webhookCounter.increment();

    log.info(
        "{} provider={} eventId={} intentId={}",
        com.chamrong.iecommerce.payment.infrastructure.logging.LogEvents.WEBHOOK_VERIFIED,
        provider,
        result.providerEventId(),
        intent.getIntentId());
  }

  private boolean isSucceeded(String eventType) {
    return "payment_intent.succeeded".equals(eventType)
        || "orders:completed".equals(eventType)
        || "PAID".equalsIgnoreCase(eventType)
        || "SUCCESS".equalsIgnoreCase(eventType);
  }

  private boolean isFailed(String eventType) {
    return "payment_intent.payment_failed".equals(eventType)
        || "orders:failed".equals(eventType)
        || "CANCELLED".equalsIgnoreCase(eventType)
        || "REFUNDED".equalsIgnoreCase(eventType)
        || "FAILURE".equalsIgnoreCase(eventType);
  }

  private PaymentTransaction.TransactionType mapToTransactionType(String eventType) {
    if (isSucceeded(eventType)) return PaymentTransaction.TransactionType.CAPTURE;
    if (isFailed(eventType)) return PaymentTransaction.TransactionType.SALE;
    return PaymentTransaction.TransactionType.AUTHORIZE;
  }
}
