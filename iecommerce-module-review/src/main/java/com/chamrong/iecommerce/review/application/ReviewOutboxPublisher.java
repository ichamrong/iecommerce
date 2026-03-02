package com.chamrong.iecommerce.review.application;

import com.chamrong.iecommerce.review.domain.ports.ReviewOutboxPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Application-level helper for writing review domain events to the outbox.
 *
 * <p>Keeps JSON serialization concerns out of individual use case handlers while preserving a clean
 * hexagonal boundary via {@link ReviewOutboxPort}.
 */
@Service
@RequiredArgsConstructor
public class ReviewOutboxPublisher {

  private static final Logger log = LoggerFactory.getLogger(ReviewOutboxPublisher.class);

  private final ReviewOutboxPort outboxPort;
  private final ObjectMapper objectMapper;

  /**
   * Serialize and append a review event to the outbox.
   *
   * @param tenantId tenant identifier
   * @param event event payload
   */
  public void publish(String tenantId, Object event) {
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("tenantId must not be null or blank for review events");
    }
    if (event == null) {
      throw new IllegalArgumentException("event must not be null");
    }
    String eventType = event.getClass().getSimpleName();
    try {
      String payload = objectMapper.writeValueAsString(event);
      outboxPort.save(tenantId, eventType, payload);
      log.debug("Queued review event type={} for tenantId={}", eventType, tenantId);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize review outbox event type={}", eventType, e);
      throw new IllegalStateException("Cannot serialize review event for outbox", e);
    }
  }
}
