package com.chamrong.iecommerce.order.infrastructure.scheduler;

import com.chamrong.iecommerce.common.EventDispatcher;
import com.chamrong.iecommerce.common.outbox.AbstractOutboxRelay;
import com.chamrong.iecommerce.order.domain.OrderOutboxEvent;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hardened version of OutboxRelayScheduler.
 *
 * <p>Key improvements for Bank-Level reliability:
 *
 * <ul>
 *   <li><b>Multi-Instance Safety:</b> Uses {@code SKIP LOCKED} to allow multiple instances of
 *       iecommerce-api to process the outbox in parallel without contention or duplication.
 *   <li><b>Exponential Backoff:</b> Uses {@code next_attempt_at} to prevent hammering downstream
 *       services during outages.
 *   <li><b>Auditability:</b> Relays {@code aggregate_id} and {@code event_type} to the
 *       EventDispatcher for better logging.
 * </ul>
 */
@Slf4j
@Component
public class HardenedOutboxRelayScheduler extends AbstractOutboxRelay<OrderOutboxEvent> {

  private final OrderOutboxRepository repository;

  public HardenedOutboxRelayScheduler(
      OrderOutboxRepository repository,
      EventDispatcher dispatcher,
      com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
    super(dispatcher, objectMapper);
    this.repository = repository;
  }

  @Override
  protected Class<?> getEventClass(String eventType) {
    // In a real app, this would use a registry. For now, we manually map order events.
    try {
      return Class.forName("com.chamrong.iecommerce.common.event." + eventType);
    } catch (ClassNotFoundException e) {
      log.error("Unknown event type: {}", eventType);
      return Object.class;
    }
  }

  @Override
  protected void saveEvent(OrderOutboxEvent event) {
    repository.save(event);
  }

  /** Runs every 2 seconds. Polls pending events that are due. */
  @Scheduled(fixedDelay = 2000)
  @Transactional
  public void relayEvents() {
    // 1. Fetch PENDING events where next_attempt_at <= now()
    // 2. Lock rows with SKIP LOCKED (postgres/mysql 8.0 support)
    List<OrderOutboxEvent> events =
        repository.findToBeProcessed(Instant.now(), PageRequest.of(0, 50));

    if (events.isEmpty()) {
      return;
    }

    log.debug("Processing {} outbox events...", events.size());

    for (OrderOutboxEvent event : events) {
      // AbstractOutboxRelay.processPendingEvents handles the actual relaying logic
      processPendingEvents(List.of(event));

      if (event.getStatus() == com.chamrong.iecommerce.common.outbox.BaseOutboxEvent.Status.SENT) {
        // Custom logic for successful relay if needed
      } else {
        // Simple backoff for non-SENT events
        int nextRetries = event.getRetryCount(); // Base class already increments retries on failure
        long delaySeconds = (long) Math.pow(2, nextRetries) * 5;
        event.updateNextAttemptAt(Instant.now().plusSeconds(delaySeconds));
        repository.save(event);
      }
    }
  }

  @Repository
  interface OrderOutboxRepository extends JpaRepository<OrderOutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        value =
            "SELECT e FROM OrderOutboxEvent e "
                + "WHERE e.status = 'PENDING' AND e.nextAttemptAt <= :now "
                + "ORDER BY e.createdAt ASC")
    @org.springframework.data.jpa.repository.QueryHints({
      @jakarta.persistence.QueryHint(
          name = "javax.persistence.lock.timeout",
          value = "-2") // SKIP LOCKED for Postgres
    })
    List<OrderOutboxEvent> findToBeProcessed(Instant now, PageRequest pageable);
  }
}
