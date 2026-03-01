package com.chamrong.iecommerce.audit.infrastructure.outbox;

import com.chamrong.iecommerce.audit.domain.event.AuditRecordedEvent;
import com.chamrong.iecommerce.audit.domain.ports.AuditPublisherPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** No-op publisher when no real outbox/Kafka is configured. */
@Component
@ConditionalOnMissingBean(AuditPublisherPort.class)
public class NoOpAuditPublisher implements AuditPublisherPort {

  @Override
  public void publish(AuditRecordedEvent event) {
    // no-op
  }
}
