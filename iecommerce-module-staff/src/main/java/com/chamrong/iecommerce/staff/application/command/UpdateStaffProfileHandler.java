package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.staff.application.StaffMapper;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.application.util.StaffSecurityContext;
import com.chamrong.iecommerce.staff.domain.StaffAuditLog;
import com.chamrong.iecommerce.staff.domain.StaffAuditLogPort;
import com.chamrong.iecommerce.staff.domain.StaffRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateStaffProfileHandler {

  private final StaffRepositoryPort staffRepository;
  private final StaffAuditLogPort auditLogPort;
  private final StaffMapper mapper;

  public StaffResponse handle(UpdateStaffCommand cmd) {
    var profile =
        staffRepository
            .findById(cmd.staffId())
            .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + cmd.staffId()));

    profile.updateProfile(cmd.fullName(), cmd.phone(), cmd.department(), cmd.branch());
    if (cmd.role() != null) profile.setRole(cmd.role());

    var saved = staffRepository.save(profile);
    auditLogPort.save(
        new StaffAuditLog(StaffSecurityContext.currentActorId(), cmd.staffId(), "STAFF_UPDATED"));
    log.info("STAFF_UPDATED: id={}", cmd.staffId());
    return mapper.toResponse(saved);
  }
}
