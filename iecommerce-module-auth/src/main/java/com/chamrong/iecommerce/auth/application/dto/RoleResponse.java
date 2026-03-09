package com.chamrong.iecommerce.auth.application.dto;

import java.time.Instant;
import java.util.List;

/**
 * Read-only DTO for role listing. Exposed by GET /api/v1/roles.
 *
 * @param id Role id (string form for API)
 * @param name Role name
 * @param description Optional description
 * @param permissions Permission names
 * @param tenantId Tenant id (null or SYSTEM for platform roles)
 * @param createdAt Created timestamp
 */
public record RoleResponse(
    String id,
    String name,
    String description,
    List<String> permissions,
    String tenantId,
    Instant createdAt) {}
