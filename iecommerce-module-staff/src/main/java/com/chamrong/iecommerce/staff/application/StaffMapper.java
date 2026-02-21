package com.chamrong.iecommerce.staff.application;

import com.chamrong.iecommerce.staff.application.dto.StaffResponse;
import com.chamrong.iecommerce.staff.domain.StaffProfile;
import org.springframework.stereotype.Component;

@Component
public class StaffMapper {

  public StaffResponse toResponse(StaffProfile profile) {
    if (profile == null) return null;
    return new StaffResponse(
        profile.getId(),
        profile.getUserId(),
        profile.getFullName(),
        profile.getPhone(),
        profile.getDepartment(),
        profile.getBranch(),
        profile.getStatus().name(),
        profile.getRole().name(),
        profile.getHireDate(),
        profile.getTerminationDate(),
        profile.getAssignedTenants());
  }
}
