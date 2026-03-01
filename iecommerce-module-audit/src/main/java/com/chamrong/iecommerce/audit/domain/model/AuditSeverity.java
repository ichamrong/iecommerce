package com.chamrong.iecommerce.audit.domain.model;

/**
 * Severity of an audit event for filtering and alerting.
 *
 * <p>INFO: routine operations. WARN: unusual or elevated risk. CRITICAL: security/financial
 * impact.
 */
public enum AuditSeverity {
  INFO,
  WARN,
  CRITICAL
}
