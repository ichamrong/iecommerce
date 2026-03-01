package com.chamrong.iecommerce.sale.application.usecase;

import com.chamrong.iecommerce.sale.application.command.OpenShiftCommand;
import com.chamrong.iecommerce.sale.application.dto.ShiftResponse;
import com.chamrong.iecommerce.sale.domain.exception.SaleDomainException;
import com.chamrong.iecommerce.sale.domain.model.Shift;
import com.chamrong.iecommerce.sale.domain.repository.ShiftRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftUseCase {

  private final ShiftRepositoryPort repository;

  @Transactional
  public ShiftResponse openShift(OpenShiftCommand command) {
    // Enforce single active shift per staff/terminal
    repository
        .findActiveShift(command.tenantId(), command.staffId(), command.terminalId())
        .ifPresent(
            s -> {
              throw new SaleDomainException("Staff already has an active shift on this terminal");
            });

    Shift shift = new Shift(command.tenantId(), command.staffId(), command.terminalId());
    return toResponse(repository.save(shift));
  }

  @Transactional
  public ShiftResponse closeShift(Long id, String tenantId) {
    Shift shift =
        repository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new EntityNotFoundException("Shift not found: " + id));

    shift.close();
    return toResponse(repository.save(shift));
  }

  private ShiftResponse toResponse(Shift s) {
    return new ShiftResponse(
        s.getId(),
        s.getStaffId(),
        s.getTerminalId(),
        s.getStartTime(),
        s.getEndTime(),
        s.getStatus().name());
  }
}
