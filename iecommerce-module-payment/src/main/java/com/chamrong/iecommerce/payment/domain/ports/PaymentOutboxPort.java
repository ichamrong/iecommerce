package com.chamrong.iecommerce.payment.domain.ports;

/**
 * Outbound port for publishing payment domain events to the outbox. Implementations persist events
 * for reliable delivery via the outbox relay.
 */
public interface PaymentOutboxPort {

  /**
   * Appends a payment event to the outbox. The event will be delivered asynchronously by the relay.
   *
   * @param tenantId tenant identifier
   * @param eventType event type (e.g. PaymentSucceededEvent, PaymentFailedEvent)
   * @param payload JSON-serialized payload
   */
  void save(String tenantId, String eventType, String payload);
}
