package com.chamrong.iecommerce.customer.infrastructure.relay;

import com.chamrong.iecommerce.common.event.EventDispatcher;
import com.chamrong.iecommerce.common.outbox.AbstractOutboxRelay;
import com.chamrong.iecommerce.customer.domain.CustomerOutboxEvent;
import com.chamrong.iecommerce.customer.domain.CustomerOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerOutboxRelayScheduler extends AbstractOutboxRelay<CustomerOutboxEvent> {

  private final CustomerOutboxRepository outboxRepository;

  public CustomerOutboxRelayScheduler(
      CustomerOutboxRepository outboxRepository,
      EventDispatcher eventPublisher,
      ObjectMapper objectMapper) {
    super(eventPublisher, objectMapper);
    this.outboxRepository = outboxRepository;
  }

  @Scheduled(fixedDelay = 5000)
  public void relay() {
    processPendingEvents(outboxRepository.findPending(50));
  }

  @Override
  protected void saveEvent(CustomerOutboxEvent event) {
    outboxRepository.save(event);
  }

  @Override
  protected Class<?> getEventClass(String eventType) {
    return switch (eventType) {
      // Add customer specific events here if needed, or use a general mapping
      default -> Object.class;
    };
  }
}
