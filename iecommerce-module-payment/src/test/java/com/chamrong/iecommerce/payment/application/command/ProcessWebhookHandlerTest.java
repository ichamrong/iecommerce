package com.chamrong.iecommerce.payment.application.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.FinancialLedgerPort;
import com.chamrong.iecommerce.payment.domain.ports.PaymentIntentRepositoryPort;
import com.chamrong.iecommerce.payment.domain.ports.WebhookVerificationPort.VerificationResult;
import com.chamrong.iecommerce.payment.domain.webhook.WebhookDeduplicationPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProcessWebhookHandlerTest {

  private PaymentIntentRepositoryPort intentRepository;
  private WebhookDeduplicationPort dedupPort;
  private FinancialLedgerPort ledgerPort;
  private SimpleMeterRegistry meterRegistry;

  private ProcessWebhookHandler handler;

  @BeforeEach
  void setUp() {
    intentRepository = Mockito.mock(PaymentIntentRepositoryPort.class);
    dedupPort = Mockito.mock(WebhookDeduplicationPort.class);
    ledgerPort = Mockito.mock(FinancialLedgerPort.class);
    meterRegistry = new SimpleMeterRegistry();
    handler = new ProcessWebhookHandler(intentRepository, dedupPort, ledgerPort, meterRegistry);
    handler.init();
  }

  @Test
  void skipsProcessingWhenEventAlreadyProcessed() {
    VerificationResult result =
        new VerificationResult(
            true, "evt-1", "payment_intent.succeeded", UUID.randomUUID().toString(), "{}", "hash");

    when(dedupPort.isAlreadyProcessed("evt-1")).thenReturn(true);

    handler.handle(ProviderType.STRIPE, result);

    verify(intentRepository, never()).findById(any());
    verify(ledgerPort, never()).record(any());
  }

  @Test
  void recordsLedgerOnSucceededEvent() {
    UUID intentId = UUID.randomUUID();
    PaymentIntent intent =
        new PaymentIntent(
            intentId,
            "t1",
            30L,
            new Money(new BigDecimal("10.00"), "USD"),
            ProviderType.STRIPE,
            "idem-1");

    VerificationResult result =
        new VerificationResult(
            true, "evt-2", "payment_intent.succeeded", intentId.toString(), "{}", "hash2");

    when(dedupPort.isAlreadyProcessed("evt-2")).thenReturn(false);
    when(intentRepository.findById(intentId)).thenReturn(Optional.of(intent));

    handler.handle(ProviderType.STRIPE, result);

    verify(ledgerPort).record(any());
    verify(intentRepository).save(intent);
  }
}
