package com.chamrong.iecommerce.sale.domain.ports;

import com.chamrong.iecommerce.sale.domain.model.Shift;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Port for shift persistence. Keyset pagination: created_at DESC, id DESC. */
public interface ShiftRepositoryPort {

  Shift save(Shift shift);

  Optional<Shift> findByIdAndTenantId(Long id, String tenantId);

  Optional<Shift> findActiveShift(String tenantId, String staffId, String terminalId);

  /**
   * Keyset page: tenant_id, then (created_at &lt; cursorCreatedAt OR (created_at = cursorCreatedAt
   * AND id &lt; cursorId)), ORDER BY created_at DESC, id DESC, LIMIT limitPlusOne.
   *
   * @param cursorCreatedAt null for first page
   * @param cursorId null for first page
   */
  List<Shift> findPage(String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);
}
