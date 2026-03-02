package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.staff.StaffSuspendedEvent;
import com.chamrong.iecommerce.staff.application.util.StaffSecurityContext;
import com.chamrong.iecommerce.staff.domain.StaffAuditActions;
import com.chamrong.iecommerce.staff.domain.StaffAuditLog;
import com.chamrong.iecommerce.staff.domain.StaffAuditLogPort;
import com.chamrong.iecommerce.staff.domain.StaffRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SuspendStaffHandler {

  private final StaffRepositoryPort staffRepository;
  private final StaffAuditLogPort auditLogPort;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long id) {
    var profile =
        staffRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
    profile.suspend();
    staffRepository.save(profile);

    auditLogPort.save(
        new StaffAuditLog(
            StaffSecurityContext.currentActorId(), id, StaffAuditActions.STAFF_SUSPENDED));
    eventPublisher.publishEvent(new StaffSuspendedEvent(null, id));
    log.info("STAFF_SUSPENDED: id={}", id);
  }
}
