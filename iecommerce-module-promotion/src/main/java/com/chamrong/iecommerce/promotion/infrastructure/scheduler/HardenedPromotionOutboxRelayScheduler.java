package com.chamrong.iecommerce.promotion.infrastructure.scheduler;

import com.chamrong.iecommerce.promotion.domain.model.PromotionOutboxEvent;
import com.chamrong.iecommerce.promotion.domain.ports.PromotionOutboxPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Scheduler to relay pending outbox events. */
@Component
@RequiredArgsConstructor
public class HardenedPromotionOutboxRelayScheduler {
  private static final Logger log =
      LoggerFactory.getLogger(HardenedPromotionOutboxRelayScheduler.class);

  private final PromotionOutboxPort outboxPort;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Scheduled(fixedDelayString = "${promotion.outbox.relay-delay:5000}")
  @Transactional
  public void relayEvents() {
    List<PromotionOutboxEvent> pending = outboxPort.findPending(10);
    if (pending.isEmpty()) {
      return;
    }

    log.debug("Relaying {} promotion outbox events", pending.size());
    for (PromotionOutboxEvent event : pending) {
      try {
        // In a real system, publish to Kafka/RabbitMQ here.
        // For this modular monolith, we use ApplicationEventPublisher for demo,
        // but usually the relay publishes to an external broker.
        applicationEventPublisher.publishEvent(event);
        event.markSent();
        outboxPort.save(event);
      } catch (Exception e) {
        log.error("Failed to relay event: {}", event.getId(), e);
        event.markFailed();
        // Exponential backoff
        event.updateNextAttemptAt(
            Instant.now().plus((long) Math.pow(2, event.getRetryCount()), ChronoUnit.MINUTES));
        outboxPort.save(event);
      }
    }
  }
}
