package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.staff.application.StaffMapper;
import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.domain.StaffProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateStaffProfileHandler {

  private final StaffProfileRepository staffProfileRepository;
  private final StaffMapper mapper;

  public StaffResponse handle(UpdateStaffCommand cmd) {
    var profile =
        staffProfileRepository
            .findById(cmd.staffId())
            .orElseThrow(() -> new EntityNotFoundException("Staff not found: " + cmd.staffId()));

    if (cmd.fullName() != null) profile.setFullName(cmd.fullName());
    if (cmd.phone() != null) profile.setPhone(cmd.phone());
    if (cmd.department() != null) profile.setDepartment(cmd.department());
    if (cmd.branch() != null) profile.setBranch(cmd.branch());
    if (cmd.role() != null) profile.setRole(cmd.role());

    var saved = staffProfileRepository.save(profile);
    return mapper.toResponse(saved);
  }
}
