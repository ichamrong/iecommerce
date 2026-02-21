package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.auth.StaffTenantsUpdatedEvent;
import com.chamrong.iecommerce.staff.application.StaffMapper;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.domain.StaffProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Replaces the full tenant assignment list for a staff member. */
@Component
@RequiredArgsConstructor
public class UpdateStaffTenantsHandler {

  private final StaffProfileRepository staffProfileRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final StaffMapper mapper;

  @Transactional
  public StaffResponse handle(UpdateStaffTenantsCommand cmd) {
    var profile =
        staffProfileRepository
            .findById(cmd.staffId())
            .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + cmd.staffId()));

    profile.setAssignedTenants(cmd.tenantCodes());
    staffProfileRepository.save(profile);

    // Notify Auth Module to sync Keycloak User Attributes
    eventPublisher.publishEvent(
        new StaffTenantsUpdatedEvent(profile.getUserId(), cmd.tenantCodes()));

    return mapper.toResponse(profile);
  }
}
