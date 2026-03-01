package com.chamrong.iecommerce.audit.domain.model;

/**
 * Outcome of the audited action: success or failure.
 *
 * <p>Used for compliance and troubleshooting (e.g. filter by FAILURE for security review).
 */
public enum AuditOutcome {
  SUCCESS,
  FAILURE
}
