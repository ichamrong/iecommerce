package com.chamrong.iecommerce.common.event;

import java.time.Instant;
import java.util.Map;

/**
 * Event published for storage-related operations (upload, download, delete, etc.). Facilitates
 * bank-level auditing and traceability across storage providers.
 */
public record StorageOperationEvent(
    String provider,
    String operation,
    String source,
    long durationMs,
    String status, // SUCCESS, FAILURE
    String errorMessage,
    Map<String, String> metadata,
    Instant timestamp) {}
