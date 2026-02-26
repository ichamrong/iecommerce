package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.PosTerminal;
import com.chamrong.iecommerce.auth.domain.PosTerminalRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPosTerminalRepository
    extends JpaRepository<PosTerminal, Long>, PosTerminalRepository {

  @Override
  List<PosTerminal> findByTenantId(String tenantId);

  @Override
  Optional<PosTerminal> findByHardwareId(String hardwareId);
}
