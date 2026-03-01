package com.chamrong.iecommerce.audit.domain.ports;

import java.time.Instant;

/**
 * Port for retention and archiving policy. Implementations may schedule jobs to archive or delete
 * events older than retention window.
 *
 * <p>Recommended: 1–2 years hot retention; archive beyond. Document in AUDIT_MODULE_SPEC.
 */
public interface AuditRetentionPolicyPort {

  /**
   * Returns the cutoff instant before which events may be archived (e.g. now minus 2 years).
   *
   * @param tenantId optional; null for global policy
   * @return cutoff instant, or null if no archiving
   */
  Instant archiveCutoff(String tenantId);

  /**
   * Returns the cutoff instant before which events may be deleted (e.g. now minus 7 years). Null
   * means never delete.
   *
   * @param tenantId optional
   * @return cutoff instant, or null
   */
  Instant deleteCutoff(String tenantId);
}
