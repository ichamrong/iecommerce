package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.auth.domain.event.StaffAccountCreatedEvent;
import com.chamrong.iecommerce.staff.StaffCreatedEvent;
import com.chamrong.iecommerce.staff.application.StaffMapper;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.domain.StaffProfile;
import com.chamrong.iecommerce.staff.domain.StaffProfileRepository;
import com.chamrong.iecommerce.staff.domain.StaffRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a platform staff profile and publishes an event for the auth module to create the user
 * account.
 */
@Component
@RequiredArgsConstructor
public class CreateStaffHandler {

  private final StaffProfileRepository staffProfileRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final StaffMapper mapper;

  @Transactional
  public StaffResponse handle(CreateStaffCommand cmd) {
    if (staffProfileRepository.existsByUserId(cmd.username())) {
      throw new IllegalArgumentException("Staff profile already exists: " + cmd.username());
    }

    // 1. Create the StaffProfile
    var profile =
        new StaffProfile(
            cmd.username(), cmd.fullName(), StaffRole.SUPPORT // default to support unless given
            );
    profile.setPhone(cmd.phone());
    profile.setDepartment(cmd.department());
    staffProfileRepository.save(profile);

    // 2. Publish event for Auth module to create the User account
    eventPublisher.publishEvent(
        new StaffAccountCreatedEvent(
            cmd.username(),
            cmd.email(),
            cmd.temporaryPassword(),
            cmd.fullName(),
            cmd.department()));

    // 3. Publish audit event
    eventPublisher.publishEvent(new StaffCreatedEvent(null, profile.getId(), cmd.email()));

    return mapper.toResponse(profile);
  }
}
