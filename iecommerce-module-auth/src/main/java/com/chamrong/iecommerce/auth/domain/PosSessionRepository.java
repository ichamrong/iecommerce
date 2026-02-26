package com.chamrong.iecommerce.auth.domain;

import java.util.List;
import java.util.Optional;

public interface PosSessionRepository {
  PosSession save(PosSession session);

  Optional<PosSession> findById(Long id);

  Optional<PosSession> findActiveSession(Long terminalId, Long cashierId);

  List<PosSession> findByTerminalId(Long terminalId);

  List<PosSession> findByTenantId(String tenantId);
}
