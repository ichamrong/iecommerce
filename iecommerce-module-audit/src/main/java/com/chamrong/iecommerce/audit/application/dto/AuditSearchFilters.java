package com.chamrong.iecommerce.audit.application.dto;

import java.time.Instant;

/**
 * Request filters for audit list endpoint (cursor-safe; included in filterHash).
 *
 * @param actorId optional
 * @param eventType optional
 * @param outcome optional
 * @param severity optional
 * @param targetType optional
 * @param targetId optional
 * @param dateFrom optional
 * @param dateTo optional
 * @param searchTerm optional
 */
public record AuditSearchFilters(
    String actorId,
    String eventType,
    String outcome,
    String severity,
    String targetType,
    String targetId,
    Instant dateFrom,
    Instant dateTo,
    String searchTerm) {

  public static AuditSearchFilters empty() {
    return new AuditSearchFilters(null, null, null, null, null, null, null, null, null);
  }
}
