package com.chamrong.iecommerce.auth.domain;

import java.util.List;
import java.util.Optional;

public interface PosTerminalRepository {
  PosTerminal save(PosTerminal terminal);

  Optional<PosTerminal> findById(Long id);

  List<PosTerminal> findByTenantId(String tenantId);

  Optional<PosTerminal> findByHardwareId(String hardwareId);
}
