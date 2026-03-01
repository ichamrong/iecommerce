package com.chamrong.iecommerce.sale.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaleOutboxRelay {

  private final JpaSaleOutboxRepository repository;
  private final ApplicationEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void relayEvents() {
    List<SaleOutboxEvent> pending = repository.findPendingForRelay(10);
    if (pending.isEmpty()) return;

    log.debug("Outbox: Found {} pending events to relay", pending.size());

    for (SaleOutboxEvent event : pending) {
      try {
        Class<?> clazz = Class.forName(event.getEventType());
        Object domainEvent = objectMapper.readValue(event.getPayload(), clazz);

        eventPublisher.publishEvent(domainEvent);

        event.setStatus(SaleOutboxEvent.OutboxStatus.SENT);
        event.setProcessedAt(Instant.now());
        repository.save(event);
      } catch (Exception e) {
        log.error("Outbox: Failed to relay event {}", event.getId(), e);
        event.setRetryCount(event.getRetryCount() + 1);
        if (event.getRetryCount() > 5) {
          event.setStatus(SaleOutboxEvent.OutboxStatus.FAILED);
        } else {
          // Simple backoff: retry in 30 * retryCount seconds
          event.setNextAttemptAt(Instant.now().plusSeconds(30L * event.getRetryCount()));
        }
        repository.save(event);
      }
    }
  }
}
