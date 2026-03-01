package com.chamrong.iecommerce.sale.domain.ports;

import com.chamrong.iecommerce.sale.domain.model.SaleSession;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Port for sale session persistence. Keyset pagination: created_at DESC, id DESC. */
public interface SaleSessionRepositoryPort {

  SaleSession save(SaleSession session);

  Optional<SaleSession> findByIdAndTenantId(Long id, String tenantId);

  Optional<SaleSession> findActiveSessionByTerminal(String tenantId, String terminalId);

  /** Keyset page. terminalId null = no filter. cursorCreatedAt/cursorId null = first page. */
  List<SaleSession> findPage(
      String tenantId, String terminalId, Instant cursorCreatedAt, Long cursorId, int limitPlusOne);
}
