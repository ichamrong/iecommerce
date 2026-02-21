package com.chamrong.iecommerce.staff.application.dto;

import java.util.Set;

/**
 * Response DTO for staff profile operations.
 *
 * @param id Internal staff profile ID
 * @param userId Username of the linked User account
 * @param fullName Display name
 * @param phone Contact phone
 * @param department Department or team
 * @param assignedTenants Tenant codes this staff member can manage
 * @param active Whether the account is active
 */
public record StaffResponse(
    Long id,
    String userId,
    String fullName,
    String phone,
    String department,
    Set<String> assignedTenants,
    boolean active) {}
