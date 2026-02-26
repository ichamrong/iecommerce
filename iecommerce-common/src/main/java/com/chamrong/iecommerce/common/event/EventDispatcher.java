package com.chamrong.iecommerce.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Dispatches events to both local ApplicationEventPublisher (for simple modular monolith flows) and
 * RabbitMQ (for bank-level reliability and async processing).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventDispatcher {

  private final ApplicationEventPublisher localPublisher;
  private final RabbitTemplate rabbitTemplate;

  public static final String EXCHANGE_NAME = "iecommerce.events";

  public void dispatch(Object event) {
    log.debug("Dispatching event: {}", event.getClass().getSimpleName());

    // 1. Local dispatch (synchronous within current context if no @Async)
    localPublisher.publishEvent(event);

    // 2. Remote dispatch (RabbitMQ)
    try {
      String routingKey = event.getClass().getSimpleName();
      rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, event);
      log.info(
          "Event {} sent to RabbitMQ with routing key {}",
          event.getClass().getSimpleName(),
          routingKey);
    } catch (Exception e) {
      log.error("Failed to send event to RabbitMQ: {}", e.getMessage());
      // In a bank-level system, we might want to throw here to trigger DB rollback if using Outbox,
      // or rely on a retry mechanism.
    }
  }
}
