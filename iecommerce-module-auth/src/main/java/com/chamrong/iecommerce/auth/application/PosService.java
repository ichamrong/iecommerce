package com.chamrong.iecommerce.auth.application;

import com.chamrong.iecommerce.auth.domain.PosSession;
import com.chamrong.iecommerce.auth.domain.PosSessionRepository;
import com.chamrong.iecommerce.auth.domain.PosTerminal;
import com.chamrong.iecommerce.auth.domain.PosTerminalRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PosService {

  private final PosTerminalRepository terminalRepository;
  private final PosSessionRepository sessionRepository;

  @Transactional
  public PosTerminal registerTerminal(
      String tenantId, String name, String hardwareId, String branchId) {
    return terminalRepository.save(new PosTerminal(tenantId, name, hardwareId, branchId));
  }

  @Transactional
  public PosSession openSession(String tenantId, Long terminalId, Long cashierId) {
    PosTerminal terminal =
        terminalRepository
            .findById(terminalId)
            .orElseThrow(() -> new IllegalArgumentException("Terminal not found"));

    if (!terminal.isActive()) {
      throw new IllegalStateException("Terminal is not active");
    }

    // Close any previous hanging sessions for this terminal/cashier
    sessionRepository
        .findActiveSession(terminalId, cashierId)
        .ifPresent(
            s -> {
              s.closeSession("Auto-closed by new login");
              sessionRepository.save(s);
            });

    return sessionRepository.save(new PosSession(tenantId, terminalId, cashierId));
  }

  @Transactional
  public void closeSession(Long sessionId, String closingNotes) {
    PosSession session =
        sessionRepository
            .findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    session.closeSession(closingNotes);
    sessionRepository.save(session);
  }

  @Transactional(readOnly = true)
  public List<PosTerminal> listTerminals(String tenantId) {
    return terminalRepository.findByTenantId(tenantId);
  }
}
