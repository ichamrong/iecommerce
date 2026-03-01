package com.chamrong.iecommerce.payment.infrastructure;

import com.chamrong.iecommerce.common.EventDispatcher;
import com.chamrong.iecommerce.common.outbox.AbstractOutboxRelay;
import com.chamrong.iecommerce.payment.domain.PaymentOutboxEvent;
import com.chamrong.iecommerce.payment.domain.PaymentOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxRelayScheduler extends AbstractOutboxRelay<PaymentOutboxEvent> {
  private static final Logger log = LoggerFactory.getLogger(PaymentOutboxRelayScheduler.class);

  private final PaymentOutboxRepository outboxRepository;
  private final Counter successCounter;
  private final Counter failureCounter;

  public PaymentOutboxRelayScheduler(
      PaymentOutboxRepository outboxRepository,
      EventDispatcher eventDispatcher,
      ObjectMapper objectMapper,
      MeterRegistry meterRegistry) {
    super(eventDispatcher, objectMapper);
    this.outboxRepository = outboxRepository;
    this.successCounter = meterRegistry.counter("payment.outbox.relay.success");
    this.failureCounter = meterRegistry.counter("payment.outbox.relay.failure");
  }

  @Scheduled(fixedDelay = 5000)
  @org.springframework.transaction.annotation.Transactional
  public void relay() {
    try {
      processPendingEvents(
          outboxRepository.findPendingForUpdate(
              org.springframework.data.domain.PageRequest.of(0, 50)));
      successCounter.increment();
    } catch (Exception e) {
      log.error("Outbox relay failed", e);
      failureCounter.increment();
      throw e;
    }
  }

  @Override
  protected void saveEvent(PaymentOutboxEvent event) {
    outboxRepository.save(event);
  }

  @Override
  protected Class<?> getEventClass(String eventType) {
    return switch (eventType) {
      case "PaymentSucceededEvent" ->
          com.chamrong.iecommerce.common.event.PaymentSucceededEvent.class;
      case "PaymentFailedEvent" -> com.chamrong.iecommerce.common.event.PaymentFailedEvent.class;
      default -> Object.class;
    };
  }
}
