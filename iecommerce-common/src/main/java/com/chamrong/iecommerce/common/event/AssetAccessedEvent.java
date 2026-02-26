package com.chamrong.iecommerce.common.event;

import java.time.Instant;

/**
 * Event published when an asset is securely accessed/downloaded. Ensures bank-level auditability of
 * file access.
 */
public record AssetAccessedEvent(
    Long assetId,
    String tenantId,
    String fileName,
    String requestedBy,
    String ipAddress,
    Instant accessedAt) {}
