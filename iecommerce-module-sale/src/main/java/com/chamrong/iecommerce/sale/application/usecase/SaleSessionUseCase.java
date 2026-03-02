package com.chamrong.iecommerce.sale.application.usecase;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.common.security.TenantGuard;
import com.chamrong.iecommerce.sale.domain.exception.SaleDomainException;
import com.chamrong.iecommerce.sale.domain.model.SaleSession;
import com.chamrong.iecommerce.sale.domain.model.Shift;
import com.chamrong.iecommerce.sale.domain.ports.AuditPort;
import com.chamrong.iecommerce.sale.domain.ports.SaleSessionRepositoryPort;
import com.chamrong.iecommerce.sale.domain.ports.ShiftRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BG6: Enforces single active session per terminal. P1: State machine guards for session
 * transitions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleSessionUseCase {

  private final SaleSessionRepositoryPort sessionRepository;
  private final ShiftRepositoryPort shiftRepository;
  private final AuditPort auditPort;

  @Transactional
  public SaleSession openSession(
      String tenantId, Long shiftId, String terminalId, String currency) {
    // BG6: Check for active session on this terminal
    sessionRepository
        .findActiveSessionByTerminal(tenantId, terminalId)
        .ifPresent(
            s -> {
              throw new SaleDomainException(
                  "Terminal "
                      + terminalId
                      + " already has an active session (ID: "
                      + s.getId()
                      + ")");
            });

    Shift shift =
        shiftRepository
            .findByIdAndTenantId(shiftId, tenantId)
            .orElseThrow(() -> new EntityNotFoundException("Shift not found: " + shiftId));
    TenantGuard.requireSameTenant(shift.getTenantId(), tenantId);

    SaleSession session = new SaleSession(shift, tenantId, terminalId, currency);
    SaleSession saved = sessionRepository.save(session);

    auditPort.log(
        tenantId,
        shift.getStaffId(),
        "OPEN_SESSION",
        "SaleSession",
        saved.getId().toString(),
        "N/A",
        null,
        saved.toString());

    return saved;
  }

  @Transactional
  public SaleSession initiateClosing(Long sessionId, String tenantId, String staffId) {
    SaleSession session =
        sessionRepository
            .findByIdAndTenantId(sessionId, tenantId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
    TenantGuard.requireSameTenant(session.getTenantId(), tenantId);

    String before = session.toString();
    session.initiateClosing();
    SaleSession saved = sessionRepository.save(session);

    auditPort.log(
        tenantId,
        staffId,
        "INITIATE_CLOSING",
        "SaleSession",
        sessionId.toString(),
        "N/A",
        before,
        session.toString());

    return saved;
  }

  @Transactional
  public SaleSession closeSession(
      Long sessionId, String tenantId, String staffId, Money actualCash) {
    SaleSession session =
        sessionRepository
            .findByIdAndTenantId(sessionId, tenantId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
    TenantGuard.requireSameTenant(session.getTenantId(), tenantId);

    String before = session.toString();
    session.close(actualCash);
    SaleSession saved = sessionRepository.save(session);

    auditPort.log(
        tenantId,
        staffId,
        "CLOSE_SESSION",
        "SaleSession",
        sessionId.toString(),
        "N/A",
        before,
        session.toString());

    return saved;
  }
}
