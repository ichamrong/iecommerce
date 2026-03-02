package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.auth.domain.event.StaffTenantsUpdatedEvent;
import com.chamrong.iecommerce.staff.application.StaffMapper;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.application.util.StaffSecurityContext;
import com.chamrong.iecommerce.staff.domain.StaffAuditActions;
import com.chamrong.iecommerce.staff.domain.StaffAuditLog;
import com.chamrong.iecommerce.staff.domain.StaffAuditLogPort;
import com.chamrong.iecommerce.staff.domain.StaffRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Replaces the full tenant assignment list for a staff member. */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateStaffTenantsHandler {

  private final StaffRepositoryPort staffRepository;
  private final StaffAuditLogPort auditLogPort;
  private final ApplicationEventPublisher eventPublisher;
  private final StaffMapper mapper;

  @Transactional
  public StaffResponse handle(UpdateStaffTenantsCommand cmd) {
    var profile =
        staffRepository
            .findById(cmd.staffId())
            .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + cmd.staffId()));

    profile.setAssignedTenants(cmd.tenantCodes());
    staffRepository.save(profile);

    eventPublisher.publishEvent(
        new StaffTenantsUpdatedEvent(profile.getUserId(), cmd.tenantCodes()));
    auditLogPort.save(
        new StaffAuditLog(
            StaffSecurityContext.currentActorId(),
            cmd.staffId(),
            StaffAuditActions.STAFF_TENANTS_UPDATED));
    log.info("STAFF_TENANTS_UPDATED: id={}", cmd.staffId());
    return mapper.toResponse(profile);
  }
}
