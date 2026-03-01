package com.chamrong.iecommerce.sale.domain.repository;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.sale.domain.model.SaleSession;
import java.util.Optional;

public interface SaleSessionRepositoryPort {

  SaleSession save(SaleSession session);

  Optional<SaleSession> findByIdAndTenantId(Long id, String tenantId);

  Optional<SaleSession> findActiveSessionByTerminal(String tenantId, String terminalId);

  CursorPage<SaleSession> findAll(String tenantId, String terminalId, String cursor, int limit);
}
