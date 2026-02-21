package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.auth.StaffAccountCreatedEvent;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.domain.StaffProfile;
import com.chamrong.iecommerce.staff.domain.StaffProfileRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a platform staff profile and publishes an event for the auth module to create the user
 * account.
 */
@Component
public class CreateStaffHandler {

  private final StaffProfileRepository staffProfileRepository;
  private final ApplicationEventPublisher eventPublisher;

  public CreateStaffHandler(
      StaffProfileRepository staffProfileRepository, ApplicationEventPublisher eventPublisher) {
    this.staffProfileRepository = staffProfileRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public StaffResponse handle(CreateStaffCommand cmd) {
    if (staffProfileRepository.existsByUserId(cmd.username())) {
      throw new IllegalArgumentException("Staff profile already exists: " + cmd.username());
    }

    // 1. Create the StaffProfile
    StaffProfile profile = new StaffProfile();
    profile.setUserId(cmd.username());
    profile.setFullName(cmd.fullName());
    profile.setPhone(cmd.phone());
    profile.setDepartment(cmd.department());
    profile.setActive(true);
    staffProfileRepository.save(profile);

    // 2. Publish event for Auth module to create the User account
    eventPublisher.publishEvent(
        new StaffAccountCreatedEvent(
            cmd.username(),
            cmd.email(),
            cmd.temporaryPassword(),
            cmd.fullName(),
            cmd.department()));

    return toResponse(profile);
  }

  public static StaffResponse toResponse(StaffProfile p) {
    return new StaffResponse(
        p.getId(),
        p.getUserId(),
        p.getFullName(),
        p.getPhone(),
        p.getDepartment(),
        p.getAssignedTenants(),
        p.isActive());
  }
}
