package com.chamrong.iecommerce.audit;

import com.chamrong.iecommerce.audit.domain.event.AuditRecordedEvent;
import com.chamrong.iecommerce.audit.domain.ports.AuditPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-level wiring for the audit publisher.
 *
 * <p>Provides a simple logging {@link AuditPublisherPort} so the application can start without a
 * full outbox/Kafka infrastructure. Events are logged at DEBUG level.
 */
@Configuration
public class AuditPublisherConfiguration {

  private static final Logger log = LoggerFactory.getLogger(AuditPublisherConfiguration.class);

  @Bean
  public AuditPublisherPort auditPublisherPort() {
    return (AuditRecordedEvent event) -> {
      if (log.isDebugEnabled()) {
        log.debug("Audit event recorded: {}", event);
      }
    };
  }
}
