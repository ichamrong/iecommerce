package com.chamrong.iecommerce.sale.application;

import com.chamrong.iecommerce.sale.application.dto.SaleSessionResponse;
import com.chamrong.iecommerce.sale.application.dto.ShiftResponse;
import com.chamrong.iecommerce.sale.domain.SaleSession;
import com.chamrong.iecommerce.sale.domain.Shift;
import com.chamrong.iecommerce.sale.domain.repository.SaleSessionRepository;
import com.chamrong.iecommerce.sale.domain.repository.ShiftRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {

  private final ShiftRepository shiftRepository;
  private final SaleSessionRepository sessionRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public ShiftResponse startShift(
      String tenantId, String staffId, String terminalId, BigDecimal openingBalance) {
    log.info("Starting shift for staff {} at terminal {}", staffId, terminalId);

    shiftRepository
        .findByStaffIdAndStatus(staffId, Shift.ShiftStatus.OPEN)
        .ifPresent(
            s -> {
              throw new IllegalStateException("Staff already has an open shift");
            });

    Shift shift = new Shift();
    shift.setTenantId(tenantId);
    shift.setStaffId(staffId);
    shift.setTerminalId(terminalId);
    shift.setStartTime(Instant.now());
    shift.setOpeningBalance(openingBalance);
    shift.setStatus(Shift.ShiftStatus.OPEN);

    return toShiftResponse(shiftRepository.save(shift));
  }

  @Transactional
  public ShiftResponse closeShift(Long shiftId, BigDecimal closingBalance) {
    log.info("Closing shift {}", shiftId);
    Shift shift =
        shiftRepository
            .findById(shiftId)
            .orElseThrow(() -> new EntityNotFoundException("Shift not found"));

    if (shift.getStatus() != Shift.ShiftStatus.OPEN) {
      throw new IllegalStateException("Shift is not open");
    }

    // Ensure all sessions under this shift are closed
    List<SaleSession> activeSessions =
        sessionRepository.findByShiftIdAndStatus(shiftId, SaleSession.SessionStatus.ACTIVE);
    if (!activeSessions.isEmpty()) {
      throw new IllegalStateException("Cannot close shift with active sale sessions");
    }

    shift.setEndTime(Instant.now());
    shift.setClosingBalance(closingBalance);
    shift.setStatus(Shift.ShiftStatus.CLOSED);

    return toShiftResponse(shiftRepository.save(shift));
  }

  @Transactional
  public SaleSessionResponse startSession(
      String tenantId, Long shiftId, String reference, String customerId) {

    Shift shift =
        shiftRepository
            .findById(shiftId)
            .filter(s -> s.getStatus() == Shift.ShiftStatus.OPEN)
            .orElseThrow(() -> new IllegalStateException("Open shift required to start session"));

    SaleSession session = new SaleSession();
    session.setTenantId(tenantId);
    session.setShift(shift);
    session.setReference(reference);
    session.setCustomerId(customerId);
    session.setStartTime(Instant.now());
    session.setStatus(SaleSession.SessionStatus.ACTIVE);

    return toSessionResponse(sessionRepository.save(session));
  }

  @Transactional
  public SaleSessionResponse endSession(Long sessionId) {
    SaleSession session =
        sessionRepository
            .findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("Session not found"));

    if (session.getStatus() != SaleSession.SessionStatus.ACTIVE) {
      throw new IllegalStateException("Session is not active");
    }

    session.setEndTime(Instant.now());
    session.setStatus(SaleSession.SessionStatus.COMPLETED);

    SaleSession saved = sessionRepository.save(session);

    eventPublisher.publishEvent(
        new com.chamrong.iecommerce.common.event.SaleSessionCompletedEvent(
            saved.getId(),
            saved.getOrderId(),
            saved.getTenantId(),
            saved.getCustomerId(),
            java.math.BigDecimal.ZERO, // Base amount until order linkage is fully implemented
            "USD",
            saved.getEndTime()));

    return toSessionResponse(saved);
  }

  private ShiftResponse toShiftResponse(Shift shift) {
    return new ShiftResponse(
        shift.getId(),
        shift.getStaffId(),
        shift.getTerminalId(),
        shift.getStartTime(),
        shift.getEndTime(),
        shift.getOpeningBalance(),
        shift.getClosingBalance(),
        shift.getStatus().name());
  }

  private SaleSessionResponse toSessionResponse(SaleSession session) {
    return new SaleSessionResponse(
        session.getId(),
        session.getShift().getId(),
        session.getCustomerId(),
        session.getOrderId(),
        session.getStartTime(),
        session.getEndTime(),
        session.getStatus().name(),
        session.getReference());
  }
}
