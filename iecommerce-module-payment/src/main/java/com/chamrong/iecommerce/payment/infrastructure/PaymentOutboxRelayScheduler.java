package com.chamrong.iecommerce.payment.infrastructure;

import com.chamrong.iecommerce.payment.domain.PaymentOutboxEvent;
import com.chamrong.iecommerce.payment.domain.PaymentOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxRelayScheduler {

  private static final int BATCH_SIZE = 50;

  private final PaymentOutboxRepository outboxRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void relay() {
    var pending = outboxRepository.findPending(BATCH_SIZE);
    if (pending.isEmpty()) return;

    for (PaymentOutboxEvent outboxEvent : pending) {
      try {
        Class<?> eventClass = getEventClass(outboxEvent.getEventType());
        var payload = objectMapper.readValue(outboxEvent.getPayload(), eventClass);
        eventPublisher.publishEvent(payload);

        outboxEvent.markSent();
        outboxRepository.save(outboxEvent);
      } catch (Exception ex) {
        outboxEvent.markFailed();
        outboxRepository.save(outboxEvent);
        log.error(
            "Payment Outbox relay: FAILED to deliver eventType={} id={}",
            outboxEvent.getEventType(),
            outboxEvent.getId(),
            ex);
      }
    }
  }

  private Class<?> getEventClass(String eventType) {
    return switch (eventType) {
      case "PaymentSucceededEvent" ->
          com.chamrong.iecommerce.common.event.PaymentSucceededEvent.class;
      case "PaymentFailedEvent" -> com.chamrong.iecommerce.common.event.PaymentFailedEvent.class;
      default -> Object.class;
    };
  }
}
