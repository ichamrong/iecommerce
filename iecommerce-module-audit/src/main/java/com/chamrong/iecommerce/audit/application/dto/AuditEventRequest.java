package com.chamrong.iecommerce.audit.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /audit/events (internal or service-to-service). Actor and tenant are
 * derived from security context; do not accept tenantId in body for scoping.
 *
 * @param eventType     stable code (e.g. ORDER_CONFIRM)
 * @param outcome       SUCCESS or FAILURE
 * @param severity      INFO, WARN, CRITICAL
 * @param targetType    e.g. ORDER, PAYMENT
 * @param targetId      stable identifier
 * @param sourceModule  calling module
 * @param sourceEndpoint optional endpoint/method
 * @param metadataJson  optional JSON; size-limited, PII-scrubbed
 */
public record AuditEventRequest(
    @NotBlank @Size(max = 128) String eventType,
    @NotNull String outcome,
    @NotNull String severity,
    @NotBlank @Size(max = 128) String targetType,
    @Size(max = 255) String targetId,
    @Size(max = 128) String sourceModule,
    @Size(max = 255) String sourceEndpoint,
    @Size(max = 8192) String metadataJson) {}
