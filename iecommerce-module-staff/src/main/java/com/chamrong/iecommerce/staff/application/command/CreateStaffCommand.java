package com.chamrong.iecommerce.staff.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Command to create a new platform staff account.
 *
 * @param fullName Display name — required
 * @param phone Contact phone — optional
 * @param department Department or team — optional
 * @param username Login username for the User account — required
 * @param email Email for the User account — required, valid format
 * @param temporaryPassword Initial password — required, staff must change on first login
 */
public record CreateStaffCommand(
    @NotBlank(message = "fullName is required") @Size(max = 255) String fullName,
    @Size(max = 50) String phone,
    @Size(max = 100) String department,
    @NotBlank(message = "username is required") @Size(max = 100) String username,
    @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 255)
        String email,
    @NotBlank(message = "temporaryPassword is required")
        @Size(min = 8, max = 100, message = "temporaryPassword must be 8–100 characters")
        String temporaryPassword) {}
