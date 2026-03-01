package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.common.EventDispatcher;
import com.chamrong.iecommerce.common.outbox.AbstractOutboxRelay;
import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import com.chamrong.iecommerce.order.domain.ports.OrderOutboxPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OutboxRelayScheduler extends AbstractOutboxRelay<OrderOutboxEvent> {

  private final OrderOutboxPort outboxRepository;

  public OutboxRelayScheduler(
      OrderOutboxPort outboxRepository,
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
  protected void saveEvent(OrderOutboxEvent event) {
    outboxRepository.save(event);
  }

  @Override
  protected Class<?> getEventClass(String eventType) {
    return switch (eventType) {
      case "OrderCompletedEvent" -> com.chamrong.iecommerce.common.event.OrderCompletedEvent.class;
      case "OrderConfirmedEvent" -> com.chamrong.iecommerce.common.event.OrderConfirmedEvent.class;
      case "OrderCancelledEvent" -> com.chamrong.iecommerce.common.event.OrderCancelledEvent.class;
      case "OrderShippedEvent" -> com.chamrong.iecommerce.common.event.OrderShippedEvent.class;
      default -> Object.class;
    };
  }
}
