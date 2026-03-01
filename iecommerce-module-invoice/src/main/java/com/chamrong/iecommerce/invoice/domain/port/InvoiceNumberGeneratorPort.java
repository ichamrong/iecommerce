package com.chamrong.iecommerce.invoice.domain.port;

/**
 * Output port: generates unique, sequential invoice numbers per tenant.
 *
 * <p>Format: {@code {PREFIX}-{YYYY}-{SEQ:06d}} — e.g., {@code ACME-2026-000042}.
 *
 * <p>Implementations must be concurrency-safe (e.g., SELECT FOR UPDATE on a counter table).
 */
public interface InvoiceNumberGeneratorPort {

  /**
   * Generates the next invoice number for the given tenant in the given calendar year.
   *
   * <p>Guaranteed: the returned number will not have been returned for the same tenant+year
   * combination in any prior or concurrent invocation.
   *
   * @param tenantId the tenant owning the sequence
   * @param year the calendar year (e.g., 2026)
   * @return unique, formatted invoice number
   */
  String next(String tenantId, int year);
}
