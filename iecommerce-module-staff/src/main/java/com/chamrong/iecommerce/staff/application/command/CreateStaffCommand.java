package com.chamrong.iecommerce.staff.application.command;

/**
 * Command to create a new platform staff account.
 *
 * @param fullName Display name of the staff member
 * @param phone Contact phone (optional)
 * @param department Department or team name (optional)
 * @param username Login username for the User account
 * @param email Email for the User account
 * @param temporaryPassword Initial password — staff should change on first login
 */
public record CreateStaffCommand(
    String fullName,
    String phone,
    String department,
    String username,
    String email,
    String temporaryPassword) {}
