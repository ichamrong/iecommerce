package com.chamrong.iecommerce.audit.application.dto;

import java.time.Instant;

/** Query parameters for filtering audit logs. */
public record AuditQuery(
    String userId,
    String action,
    String resourceType,
    String resourceId,
    String searchTerm,
    Instant from,
    Instant to) {}
