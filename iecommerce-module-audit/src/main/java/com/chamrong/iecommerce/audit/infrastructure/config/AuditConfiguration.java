package com.chamrong.iecommerce.audit.infrastructure.config;

import com.chamrong.iecommerce.audit.domain.ports.AuditRetentionPolicyPort;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Audit module configuration. Retention policy default: no archiving (override for production).
 */
@Configuration
public class AuditConfiguration {

  /** Default retention: no archiving. Override with a real implementation if needed. */
  @Bean
  public AuditRetentionPolicyPort auditRetentionPolicyPort() {
    return new AuditRetentionPolicyPort() {
      @Override
      public Instant archiveCutoff(String tenantId) {
        return null;
      }

      @Override
      public Instant deleteCutoff(String tenantId) {
        return null;
      }
    };
  }
}
