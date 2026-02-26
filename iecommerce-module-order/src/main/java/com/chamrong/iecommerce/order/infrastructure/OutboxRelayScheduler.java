package com.chamrong.iecommerce.order.infrastructure;

import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import com.chamrong.iecommerce.order.domain.OrderOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox Relay — the "postman" that reads PENDING rows from the outbox table and publishes them as
 * Spring Application Events.
 *
 * <p><b>How this guarantees no lost events (even in a crash):</b>
 *
 * <ol>
 *   <li>OrderService saves the Order AND inserts an outbox row in ONE ACID transaction.
 *   <li>Server crashes? PostgreSQL WAL rolls back or replays to ensure both are written.
 *   <li>Server restarts? This scheduler runs and finds the PENDING row and delivers it.
 *   <li>This is the Outbox Pattern: at-least-once delivery with exactly-once DB consistency.
 * </ol>
 *
 * <p>Spring tools used here:
 *
 * <ul>
 *   <li>{@link Scheduled} — polls every 5 seconds with a fixed-rate cron.
 *   <li>{@link Transactional} — marking the event SENT is itself a TX so partial updates never
 *       leave rows in an inconsistent state.
 *   <li>{@link ApplicationEventPublisher} — decoupled from Kafka/RabbitMQ, works locally and can be
 *       swapped for a real broker without changing business logic.
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

  private static final int BATCH_SIZE = 50;

  private final OrderOutboxRepository outboxRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 5000) // poll every 5 seconds after last completion
  @Transactional
  public void relay() {
    var pending = outboxRepository.findPending(BATCH_SIZE);
    if (pending.isEmpty()) return;

    log.debug("Outbox relay: processing {} pending event(s)", pending.size());

    for (OrderOutboxEvent outboxEvent : pending) {
      try {
        // Map logical event types to concrete classes for correct deserialization.
        // This ensures downstream @EventListeners receive the actual object type.
        Class<?> eventClass = getEventClass(outboxEvent.getEventType());
        var payload = objectMapper.readValue(outboxEvent.getPayload(), eventClass);
        eventPublisher.publishEvent(payload);

        outboxEvent.markSent();
        outboxRepository.save(outboxEvent);
        log.info(
            "Outbox relay: delivered eventType={} id={}",
            outboxEvent.getEventType(),
            outboxEvent.getId());

      } catch (Exception ex) {
        outboxEvent.markFailed();
        outboxRepository.save(outboxEvent);
        log.error(
            "Outbox relay: FAILED to deliver eventType={} id={} retries={}",
            outboxEvent.getEventType(),
            outboxEvent.getId(),
            outboxEvent.getRetryCount(),
            ex);
      }
    }
  }

  private Class<?> getEventClass(String eventType) {
    return switch (eventType) {
      case "OrderCompletedEvent" -> com.chamrong.iecommerce.common.event.OrderCompletedEvent.class;
      case "OrderConfirmedEvent" -> com.chamrong.iecommerce.common.event.OrderConfirmedEvent.class;
      case "OrderCancelledEvent" -> com.chamrong.iecommerce.common.event.OrderCancelledEvent.class;
      case "OrderShippedEvent" -> com.chamrong.iecommerce.common.event.OrderShippedEvent.class;
      default -> Object.class;
    };
  }
}
