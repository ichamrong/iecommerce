package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.PosSession;
import com.chamrong.iecommerce.auth.domain.ports.PosSessionRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPosSessionRepository
    extends JpaRepository<PosSession, Long>, PosSessionRepositoryPort {

  @Override
  @Query(
      "SELECT s FROM PosSession s WHERE s.terminalId = :terminalId AND s.cashierId = :cashierId AND"
          + " s.active = true")
  Optional<PosSession> findActiveSession(
      @Param("terminalId") Long terminalId, @Param("cashierId") Long cashierId);

  @Override
  List<PosSession> findByTerminalId(Long terminalId);

  @Override
  List<PosSession> findByTenantId(String tenantId);
}
