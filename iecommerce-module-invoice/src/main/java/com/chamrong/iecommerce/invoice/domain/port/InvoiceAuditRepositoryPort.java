package com.chamrong.iecommerce.invoice.domain.port;

import com.chamrong.iecommerce.invoice.domain.InvoiceAuditEntry;
import java.time.Instant;
import java.util.List;

/**
 * Output port: append-only audit log for invoice lifecycle events.
 *
 * <p>ASVS V8.2 — Critical business operations must be logged with actor and timestamp.
 */
public interface InvoiceAuditRepositoryPort {

  /**
   * Appends an audit entry. Implementations must never update or delete entries.
   *
   * @param entry the entry to append
   */
  void append(InvoiceAuditEntry entry);

  /**
   * Cursor-keyset paginated query of audit entries for an invoice.
   *
   * <p>Stable sort: {@code (occurred_at DESC, id DESC)}.
   *
   * @param invoiceId invoice to query
   * @param tenantId used to enforce tenant scoping at the query level
   * @param afterOccurredAt cursor timestamp of last seen row; null for first page
   * @param afterId cursor id of last seen row; null for first page
   * @param limit max entries to return; callers cap at 100
   */
  List<InvoiceAuditEntry> findByInvoiceCursor(
      Long invoiceId, String tenantId, Instant afterOccurredAt, Long afterId, int limit);
}
