package com.chamrong.iecommerce.staff.application.command;

import com.chamrong.iecommerce.staff.domain.StaffRole;

public record UpdateStaffCommand(
    Long staffId,
    String fullName,
    String phone,
    String department,
    String branch,
    StaffRole role) {}
