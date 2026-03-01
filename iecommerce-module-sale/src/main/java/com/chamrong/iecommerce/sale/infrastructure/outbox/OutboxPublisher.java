package com.chamrong.iecommerce.sale.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisher {

  private final JpaSaleOutboxRepository repository;
  private final ObjectMapper objectMapper;

  @Transactional(propagation = Propagation.MANDATORY)
  public void publish(String tenantId, Object event, Long aggregateId) {
    try {
      String type = event.getClass().getName();
      String payload = objectMapper.writeValueAsString(event);
      String traceId = null; // Could get from MDC if available

      SaleOutboxEvent outboxEvent =
          new SaleOutboxEvent(tenantId, type, payload, aggregateId, traceId);
      repository.save(outboxEvent);
      log.debug("Outbox: Event {} persisted for aggregate {}", type, aggregateId);
    } catch (Exception e) {
      log.error("Outbox: Failed to persist event", e);
      throw new RuntimeException("Could not persist event to outbox", e);
    }
  }
}
