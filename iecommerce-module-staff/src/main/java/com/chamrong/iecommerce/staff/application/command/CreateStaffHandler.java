package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.auth.domain.event.StaffAccountCreatedEvent;
import com.chamrong.iecommerce.staff.StaffCreatedEvent;
import com.chamrong.iecommerce.staff.application.StaffMapper;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.application.util.StaffSecurityContext;
import com.chamrong.iecommerce.staff.domain.StaffAuditLog;
import com.chamrong.iecommerce.staff.domain.StaffAuditLogPort;
import com.chamrong.iecommerce.staff.domain.StaffProfile;
import com.chamrong.iecommerce.staff.domain.StaffRepositoryPort;
import com.chamrong.iecommerce.staff.domain.StaffRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateStaffHandler {

  private final StaffRepositoryPort staffRepository;
  private final StaffAuditLogPort auditLogPort;
  private final ApplicationEventPublisher eventPublisher;
  private final StaffMapper mapper;

  @Transactional
  public StaffResponse handle(CreateStaffCommand cmd) {
    if (staffRepository.existsByUserId(cmd.username())) {
      log.warn("STAFF_CREATE_CONFLICT: email={}", maskEmail(cmd.email()));
      throw new IllegalArgumentException("Staff profile already exists: " + cmd.username());
    }

    var profile = new StaffProfile(cmd.username(), cmd.fullName(), StaffRole.SUPPORT);
    profile.setPhone(cmd.phone());
    profile.setDepartment(cmd.department());
    staffRepository.save(profile);

    eventPublisher.publishEvent(
        new StaffAccountCreatedEvent(
            cmd.username(),
            cmd.email(),
            cmd.temporaryPassword(),
            cmd.fullName(),
            cmd.department()));
    eventPublisher.publishEvent(new StaffCreatedEvent(null, profile.getId(), cmd.email()));

    auditLogPort.save(
        new StaffAuditLog(StaffSecurityContext.currentActorId(), profile.getId(), "STAFF_CREATED"));
    log.info("STAFF_CREATED: id={}, email={}", profile.getId(), maskEmail(cmd.email()));
    return mapper.toResponse(profile);
  }

  private String maskEmail(String email) {
    if (email == null || !email.contains("@")) return "***";
    int at = email.indexOf('@');
    return email.substring(0, Math.min(2, at)) + "***" + email.substring(at);
  }
}
