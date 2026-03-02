package com.chamrong.iecommerce.review.domain.ports;

/**
 * Outbound port for publishing review domain events to the outbox.
 *
 * <p>Implementations persist events for reliable delivery via the outbox relay.
 */
public interface ReviewOutboxPort {

  /**
   * Append a review-related event to the outbox.
   *
   * @param tenantId tenant identifier
   * @param eventType event type (for example ReviewSubmittedEvent, ReviewApprovedEvent)
   * @param payload JSON-serialized payload
   */
  void save(String tenantId, String eventType, String payload);
}
