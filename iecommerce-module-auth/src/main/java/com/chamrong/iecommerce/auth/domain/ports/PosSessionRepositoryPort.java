package com.chamrong.iecommerce.auth.domain.ports;

import com.chamrong.iecommerce.auth.domain.PosSession;
import java.util.List;
import java.util.Optional;

/** Port for POS session persistence. Implemented by infrastructure adapters. */
public interface PosSessionRepositoryPort {

  PosSession save(PosSession session);

  Optional<PosSession> findById(Long id);

  Optional<PosSession> findActiveSession(Long terminalId, Long cashierId);

  List<PosSession> findByTerminalId(Long terminalId);

  List<PosSession> findByTenantId(String tenantId);
}
