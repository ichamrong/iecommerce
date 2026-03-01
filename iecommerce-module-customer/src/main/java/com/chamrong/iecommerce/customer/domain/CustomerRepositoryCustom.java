package com.chamrong.iecommerce.customer.domain;

import com.chamrong.iecommerce.customer.api.util.CursorEncoder.Cursor;
import java.util.List;

public interface CustomerRepositoryCustom {
  /**
   * Keyset paginated list for a tenant. Sort: created_at DESC, id DESC.
   *
   * @param tenantId tenant scope (required)
   * @param cursor null for first page
   * @param limit page size
   */
  List<Customer> findNextPage(String tenantId, Cursor cursor, int limit);

  /** All customers for a tenant (use for small datasets; prefer findNextPage for listing). */
  List<Customer> findAllByTenantId(String tenantId);
}
