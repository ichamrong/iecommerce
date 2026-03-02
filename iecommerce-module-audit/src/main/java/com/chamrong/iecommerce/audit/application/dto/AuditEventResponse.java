package com.chamrong.iecommerce.audit.application.dto;

import java.time.Instant;

/**
 * Single audit event response (GET /audit/events/{id}).
 *
 * @param id event id
 * @param tenantId tenant (exposed only when needed for admin)
 * @param createdAt when recorded
 * @param correlationId request/trace id
 * @param actorId who performed the action
 * @param actorType USER, SYSTEM, etc.
 * @param actorRole optional role
 * @param eventType stable code
 * @param outcome SUCCESS, FAILURE
 * @param severity INFO, WARN, CRITICAL
 * @param targetType e.g. ORDER
 * @param targetId target identifier
 * @param sourceModule module
 * @param sourceEndpoint endpoint
 * @param ipAddress optional
 * @param userAgent optional
 * @param metadataJson optional
 * @param hashValid whether tamper verification passed (for verify endpoint)
 */
public record AuditEventResponse(
    Long id,
    String tenantId,
    Instant createdAt,
    String correlationId,
    String actorId,
    String actorType,
    String actorRole,
    String eventType,
    String outcome,
    String severity,
    String targetType,
    String targetId,
    String sourceModule,
    String sourceEndpoint,
    String ipAddress,
    String userAgent,
    String metadataJson,
    Boolean hashValid) {}
