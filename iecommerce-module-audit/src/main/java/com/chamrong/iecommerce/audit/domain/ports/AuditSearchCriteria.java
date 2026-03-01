package com.chamrong.iecommerce.audit.domain.ports;

import java.time.Instant;

/**
 * Criteria for filtering audit events (cursor-safe; used in filterHash).
 *
 * @param actorId    optional
 * @param eventType  optional
 * @param outcome    optional
 * @param severity   optional
 * @param targetType optional
 * @param targetId   optional
 * @param dateFrom   optional
 * @param dateTo     optional
 * @param searchTerm optional free text (metadata/action); use with care
 */
public record AuditSearchCriteria(
    String actorId,
    String eventType,
    String outcome,
    String severity,
    String targetType,
    String targetId,
    Instant dateFrom,
    Instant dateTo,
    String searchTerm) {

  public static AuditSearchCriteria empty() {
    return new AuditSearchCriteria(null, null, null, null, null, null, null, null, null);
  }
}
