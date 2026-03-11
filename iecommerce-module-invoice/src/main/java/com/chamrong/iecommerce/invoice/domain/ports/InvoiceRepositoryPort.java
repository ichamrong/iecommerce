package com.chamrong.iecommerce.invoice.domain.ports;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Output port: persistence operations for the {@link Invoice} aggregate.
 *
 * <p>All find operations are tenant-scoped — implementations MUST enforce tenant isolation.
 */
public interface InvoiceRepositoryPort {

  /**
   * Saves (insert or update) an invoice.
   *
   * @param invoice the aggregate to persist
   * @return the saved instance (may differ if IDs are assigned on insert)
   */
  Invoice save(Invoice invoice);

  /**
   * Finds an invoice by ID enforcing tenant ownership.
   *
   * <p>ASVS V4.2 — Returns {@link Optional#empty()} (not 403) when the invoice belongs to a
   * different tenant, preventing object-level enumeration.
   *
   * @param id the invoice primary key
   * @param tenantId the calling tenant
   */
  Optional<Invoice> findByIdAndTenant(Long id, String tenantId);

  /**
   * Checks whether a given invoice number is already in use for a tenant.
   *
   * @param tenantId the tenant
   * @param invoiceNumber the number to check
   */
  boolean existsByTenantAndInvoiceNumber(String tenantId, String invoiceNumber);

  /**
   * Cursor-keyset paginated listing of invoices for a tenant.
   *
   * <p>Stable sort: {@code (issue_date DESC, id DESC)}.
   *
   * @param tenantId the tenant scope
   * @param statusFilter optional status filter, null means all statuses
   * @param afterIssuedAt cursor: issue_date of the last seen item; null for first page
   * @param afterId cursor: id of the last seen item; null for first page
   * @param limit maximum items to return; capped at 100 by callers
   */
  List<Invoice> findByTenantCursor(
      String tenantId, InvoiceStatus statusFilter, Instant afterIssuedAt, Long afterId, int limit);

  /**
   * Cursor-keyset paginated listing of invoices across all tenants (platform admin only).
   *
   * <p>Same stable sort: {@code (issue_date DESC, id DESC)}. Call only when caller is platform
   * admin and JWT has no tenant_id.
   *
   * @param statusFilter optional status filter, null means all statuses
   * @param afterIssuedAt cursor: issue_date of the last seen item; null for first page
   * @param afterId cursor: id of the last seen item; null for first page
   * @param limit maximum items to return; capped at 100 by callers
   */
  List<Invoice> findByCursorAllTenants(
      InvoiceStatus statusFilter, Instant afterIssuedAt, Long afterId, int limit);

  /**
   * Finds an invoice by human-readable invoice number, enforcing tenant scoping.
   *
   * <p>ASVS V4.2 — Returns empty Optional when the number belongs to a different tenant. Used by
   * the {@code POST /invoices/verify} endpoint for signature block verification.
   *
   * @param invoiceNumber e.g. {@code ACME-2026-000042}
   * @param tenantId the calling tenant
   */
  Optional<Invoice> findByInvoiceNumberAndTenant(String invoiceNumber, String tenantId);

  /** Finds all invoices for a given order (cross-tenant if needed by admin; use with care). */
  List<Invoice> findByOrderId(Long orderId);
}
