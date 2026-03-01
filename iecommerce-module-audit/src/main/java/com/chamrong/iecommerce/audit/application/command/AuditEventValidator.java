package com.chamrong.iecommerce.audit.application.command;

import com.chamrong.iecommerce.audit.application.dto.AuditEventRequest;
import com.chamrong.iecommerce.audit.domain.exception.AuditDomainException;
import com.chamrong.iecommerce.audit.domain.model.AuditOutcome;
import com.chamrong.iecommerce.audit.domain.policy.AuditPolicy;
import com.chamrong.iecommerce.audit.domain.model.AuditSeverity;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * Validates audit event request: outcome/severity enum, metadata size, PII rules.
 */
@Component
public class AuditEventValidator {

  /**
   * Validates the request. Throws AuditDomainException if invalid.
   *
   * @param request request to validate
   * @throws AuditDomainException if validation fails
   */
  public void validate(AuditEventRequest request) {
    if (request == null) {
      throw new AuditDomainException("AUDIT_VALIDATION", "Request must not be null");
    }
    try {
      AuditOutcome.valueOf(request.outcome());
    } catch (IllegalArgumentException e) {
      throw new AuditDomainException(
          "AUDIT_VALIDATION", "Invalid outcome: " + request.outcome() + "; use SUCCESS or FAILURE");
    }
    try {
      AuditSeverity.valueOf(request.severity());
    } catch (IllegalArgumentException e) {
      throw new AuditDomainException(
          "AUDIT_VALIDATION",
          "Invalid severity: " + request.severity() + "; use INFO, WARN, or CRITICAL");
    }
    if (request.metadataJson() != null) {
      int bytes = request.metadataJson().getBytes(StandardCharsets.UTF_8).length;
      if (bytes > AuditPolicy.METADATA_JSON_MAX_BYTES) {
        throw new AuditDomainException(
            "AUDIT_VALIDATION",
            "metadataJson exceeds max size: " + AuditPolicy.METADATA_JSON_MAX_BYTES + " bytes");
      }
    }
  }
}
