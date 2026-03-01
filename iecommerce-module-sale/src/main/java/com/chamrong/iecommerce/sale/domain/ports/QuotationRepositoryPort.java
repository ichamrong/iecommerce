package com.chamrong.iecommerce.sale.domain.ports;

import com.chamrong.iecommerce.sale.domain.model.Quotation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Port for quotation persistence. Keyset pagination: created_at DESC, id DESC. */
public interface QuotationRepositoryPort {

  Quotation save(Quotation quotation);

  Optional<Quotation> findByIdAndTenantId(Long id, String tenantId);

  List<Quotation> findPage(
      String tenantId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);

  /**
   * Atomic transition: confirms quotation ONLY if in DRAFT/SENT state and belongs to tenant.
   *
   * @return true if updated, false otherwise
   */
  boolean confirmQuotation(String tenantId, Long id);
}
