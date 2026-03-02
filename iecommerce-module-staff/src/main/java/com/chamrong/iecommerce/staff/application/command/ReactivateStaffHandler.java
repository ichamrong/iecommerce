package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.staff.StaffReactivatedEvent;
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
public class ReactivateStaffHandler {

  private final StaffRepositoryPort staffRepository;
  private final StaffAuditLogPort auditLogPort;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(Long id) {
    var profile =
        staffRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + id));
    profile.reactivate();
    staffRepository.save(profile);

    auditLogPort.save(
        new StaffAuditLog(
            StaffSecurityContext.currentActorId(), id, StaffAuditActions.STAFF_REACTIVATED));
    eventPublisher.publishEvent(new StaffReactivatedEvent(null, id));
    log.info("STAFF_REACTIVATED: id={}", id);
  }
}
