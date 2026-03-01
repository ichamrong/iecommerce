package com.chamrong.iecommerce.auth.domain.ports;

import com.chamrong.iecommerce.auth.domain.PosTerminal;
import java.util.List;
import java.util.Optional;

/** Port for POS terminal persistence. Implemented by infrastructure adapters. */
public interface PosTerminalRepositoryPort {

  PosTerminal save(PosTerminal terminal);

  Optional<PosTerminal> findById(Long id);

  List<PosTerminal> findByTenantId(String tenantId);

  Optional<PosTerminal> findByHardwareId(String hardwareId);
}
