package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.staff.domain.StaffRole;
import jakarta.validation.constraints.Size;

public record UpdateStaffCommand(
    Long staffId,
    @Size(max = 255) String fullName,
    @Size(max = 50) String phone,
    @Size(max = 100) String department,
    @Size(max = 100) String branch,
    StaffRole role) {}
