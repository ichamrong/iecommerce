package com.chamrong.iecommerce.promotion.application.service;

import com.chamrong.iecommerce.promotion.domain.event.PromotionEventPublisher;
import com.chamrong.iecommerce.promotion.domain.model.PromotionOutboxEvent;
import com.chamrong.iecommerce.promotion.domain.port.PromotionOutboxPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Service to publish events via Outbox pattern. */
@Service
public class PromotionEventPublisherImpl implements PromotionEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(PromotionEventPublisherImpl.class);

  private final PromotionOutboxPort outboxPort;
  private final ObjectMapper objectMapper;

  public PromotionEventPublisherImpl(PromotionOutboxPort outboxPort, ObjectMapper objectMapper) {
    this.outboxPort = outboxPort;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void publish(String tenantId, String eventType, Long aggregateId, Object payload) {
    try {
      String jsonPayload = objectMapper.writeValueAsString(payload);
      PromotionOutboxEvent event =
          PromotionOutboxEvent.pending(tenantId, eventType, jsonPayload, aggregateId);
      outboxPort.save(event);
      log.debug("Recorded outbox event: {} for aggregate: {}", eventType, aggregateId);
    } catch (Exception e) {
      log.error("Failed to serialize event payload for {}: {}", eventType, aggregateId, e);
      throw new RuntimeException("Event serialization failed", e);
    }
  }
}
