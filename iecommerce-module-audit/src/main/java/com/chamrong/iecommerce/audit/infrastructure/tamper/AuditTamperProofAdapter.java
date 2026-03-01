package com.chamrong.iecommerce.audit.infrastructure.tamper;

import com.chamrong.iecommerce.audit.domain.model.AuditEvent;
import com.chamrong.iecommerce.audit.domain.ports.AuditTamperProofPort;
import com.chamrong.iecommerce.audit.domain.service.AuditDomainService;
import org.springframework.stereotype.Component;

/**
 * Tamper-evidence adapter using hash chain: hash(canonical_form) and chain link verification.
 */
@Component
public class AuditTamperProofAdapter implements AuditTamperProofPort {

  @Override
  public String computeHash(AuditEvent event) {
    String canonical = AuditDomainService.toCanonicalForm(event);
    return AuditDomainService.computeHash(canonical);
  }

  @Override
  public boolean verifyEventHash(AuditEvent event) {
    if (event.getHash() == null || event.getHash().isEmpty()) return false;
    String canonical = AuditDomainService.toCanonicalForm(event);
    String computed = AuditDomainService.computeHash(canonical);
    return computed.equals(event.getHash());
  }

  @Override
  public boolean verifyChainLink(AuditEvent event, String prevEventHash) {
    if (event.getPrevHash() == null || event.getPrevHash().isEmpty()) {
      return prevEventHash == null || prevEventHash.isEmpty();
    }
    return event.getPrevHash().equals(prevEventHash);
  }
}
