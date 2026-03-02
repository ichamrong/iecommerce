package com.chamrong.iecommerce.booking.domain.ports;

/** Port for publishing domain events via outbox (at-least-once). */
public interface OutboxPublisherPort {

  /**
   * Publishes an event to the outbox. Must be called in the same transaction as the aggregate save.
   *
   * @param tenantId tenant
   * @param eventType event type (e.g. BOOKING_CONFIRMED)
   * @param aggregateId booking id
   * @param payload JSON payload
   */
  void publish(String tenantId, String eventType, Long aggregateId, String payload);
}
