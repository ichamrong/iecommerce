package com.chamrong.iecommerce.audit.application.dto;

import java.time.Instant;

/** DTO for audit log entry. */
public record AuditResponse(
    Long id,
    String userId,
    String action,
    String resourceType,
    String resourceId,
    String metadata,
    String ipAddress,
    String userAgent,
    Instant timestamp) {}
