package com.chamrong.iecommerce.staff.application.command;

import java.util.Set;

/**
 * Command to replace the full set of assigned tenants for a staff member.
 *
 * @param staffId ID of the StaffProfile to update
 * @param tenantCodes Full replacement set of tenant codes (replaces previous assignment)
 */
public record UpdateStaffTenantsCommand(Long staffId, Set<String> tenantCodes) {}
