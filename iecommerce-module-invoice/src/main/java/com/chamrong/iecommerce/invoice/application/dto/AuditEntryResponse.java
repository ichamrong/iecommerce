package com.chamrong.iecommerce.invoice.application.dto;

import com.chamrong.iecommerce.invoice.domain.InvoiceAuditEntry.Action;
import java.time.Instant;

/**
 * Read model for a single invoice audit log entry.
 *
 * @param id entry primary key
 * @param action lifecycle action
 * @param actorId who performed the action
 * @param details JSON context — never contains PII or keys
 * @param occurredAt when the action happened
 */
public record AuditEntryResponse(
    Long id, Action action, String actorId, String details, Instant occurredAt) {}
