package com.chamrong.iecommerce.audit.domain.ports;

import com.chamrong.iecommerce.audit.domain.event.AuditRecordedEvent;

/**
 * Optional port for publishing audit events (e.g. outbox, Kafka). Implementations may be no-op.
 */
public interface AuditPublisherPort {

  /**
   * Publishes an audit recorded event. Best-effort; should not fail the main write.
   *
   * @param event domain event after persist
   */
  void publish(AuditRecordedEvent event);
}
