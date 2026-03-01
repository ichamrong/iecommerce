package com.chamrong.iecommerce.audit.domain.ports;

import com.chamrong.iecommerce.audit.domain.model.AuditEvent;

/**
 * Port for tamper-evidence: compute and verify hash chain (per-tenant).
 *
 * <p>Option A: hash chain — each event stores prevHash + hash(canonical_fields + prevHash).
 * Verification: recompute hash from stored fields and compare; verify prevHash links to previous
 * event.
 */
public interface AuditTamperProofPort {

  /**
   * Computes the hash for the given event (canonical form + prevHash). Used when persisting.
   *
   * @param event event with prevHash set
   * @return hex hash string
   */
  String computeHash(AuditEvent event);

  /**
   * Verifies a single event: recomputes hash from stored fields and compares to stored hash.
   *
   * @param event persisted event with id
   * @return true if stored hash matches recomputed
   */
  boolean verifyEventHash(AuditEvent event);

  /**
   * Verifies chain link: event.prevHash equals previous event's hash. Call after verifyEventHash.
   *
   * @param event       current event
   * @param prevEventHash hash of the previous event in tenant order (null if first)
   * @return true if chain link is valid
   */
  boolean verifyChainLink(AuditEvent event, String prevEventHash);
}
