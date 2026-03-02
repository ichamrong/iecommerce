package com.chamrong.iecommerce.payment.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.PaymentIntent;
import com.chamrong.iecommerce.payment.domain.ProviderType;
import com.chamrong.iecommerce.payment.domain.ports.PaymentIntentRepositoryPort;
import com.chamrong.iecommerce.payment.domain.ports.PaymentProviderPort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class CreatePaymentIntentHandlerTest {

  private PaymentIntentRepositoryPort repository;
  private PaymentProviderPort provider;
  private CreatePaymentIntentHandler handler;

  @BeforeEach
  void setUp() {
    repository = Mockito.mock(PaymentIntentRepositoryPort.class);
    provider = Mockito.mock(PaymentProviderPort.class);
    when(provider.supports(ProviderType.STRIPE)).thenReturn(true);
    handler = new CreatePaymentIntentHandler(repository, List.of(provider));
  }

  @Test
  void returnsExistingIntentForSameIdempotencyKey() {
    String tenantId = "t1";
    String idem = "key-123";
    PaymentIntent existing =
        new PaymentIntent(
            UUID.randomUUID(),
            tenantId,
            10L,
            new Money(new BigDecimal("100.00"), "USD"),
            ProviderType.STRIPE,
            idem);

    when(repository.findByIdempotencyKey(tenantId, idem)).thenReturn(Optional.of(existing));

    CreatePaymentIntentHandler.Command cmd =
        new CreatePaymentIntentHandler.Command(
            tenantId,
            10L,
            new Money(new BigDecimal("100.00"), "USD"),
            ProviderType.STRIPE,
            idem,
            "https://return",
            "https://cancel");

    PaymentIntent result = handler.handle(cmd);

    assertEquals(existing.getIntentId(), result.getIntentId());
    verify(provider, never()).createIntent(any());
    verify(repository, never()).save(any());
  }

  @Test
  void createsNewIntentWhenNoExistingIdempotencyRecord() {
    String tenantId = "t1";
    String idem = "key-new";

    when(repository.findByIdempotencyKey(tenantId, idem)).thenReturn(Optional.empty());
    PaymentProviderPort.ProviderResponse response =
        new PaymentProviderPort.ProviderResponse(
            "ext-1", "https://checkout", "secret", "REQUIRES_ACTION", null, null, null, null);
    when(provider.createIntent(any())).thenReturn(response);

    CreatePaymentIntentHandler.Command cmd =
        new CreatePaymentIntentHandler.Command(
            tenantId,
            20L,
            new Money(new BigDecimal("50.00"), "USD"),
            ProviderType.STRIPE,
            idem,
            "https://return",
            "https://cancel");

    handler.handle(cmd);

    ArgumentCaptor<PaymentIntent> captor = ArgumentCaptor.forClass(PaymentIntent.class);
    verify(repository).save(captor.capture());
    PaymentIntent saved = captor.getValue();

    assertEquals(tenantId, saved.getTenantId());
    assertEquals(20L, saved.getOrderId());
    assertEquals(idem, saved.getIdempotencyKey());
  }
}
