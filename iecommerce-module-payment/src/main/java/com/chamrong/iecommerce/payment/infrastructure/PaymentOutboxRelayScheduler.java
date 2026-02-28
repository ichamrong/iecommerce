package com.chamrong.iecommerce.payment.infrastructure;

import com.chamrong.iecommerce.common.EventDispatcher;
import com.chamrong.iecommerce.common.outbox.AbstractOutboxRelay;
import com.chamrong.iecommerce.payment.domain.PaymentOutboxEvent;
import com.chamrong.iecommerce.payment.domain.PaymentOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentOutboxRelayScheduler extends AbstractOutboxRelay<PaymentOutboxEvent> {

  private final PaymentOutboxRepository outboxRepository;

  public PaymentOutboxRelayScheduler(
      PaymentOutboxRepository outboxRepository,
      EventDispatcher eventDispatcher,
      ObjectMapper objectMapper) {
    super(eventDispatcher, objectMapper);
    this.outboxRepository = outboxRepository;
  }

  @Scheduled(fixedDelay = 5000)
  @org.springframework.transaction.annotation.Transactional
  public void relay() {
    processPendingEvents(outboxRepository.findPending(50));
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
