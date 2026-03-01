package com.chamrong.iecommerce.sale.domain.repository;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.sale.domain.model.Quotation;
import java.util.Optional;

public interface QuotationRepositoryPort {

  Quotation save(Quotation quotation);

  Optional<Quotation> findByIdAndTenantId(Long id, String tenantId);

  CursorPage<Quotation> findAll(String tenantId, String cursor, int limit);

  /**
   * Atomic transition: confirms quotation ONLY if in DRAFT/SENT state and belongs to tenant.
   * returns true if updated, false otherwise.
   */
  boolean confirmQuotation(String tenantId, Long id);
}
