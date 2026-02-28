package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.common.annotation.Masked;
import org.springframework.lang.Nullable;

/**
 * Command to register a new user within a specific tenant.
 *
 * @param username unique login name
 * @param email unique email address
 * @param password raw (unhashed) password
 * @param tenantId the tenant this user belongs to
 */
public record RegisterCommand(
    String username,
    String email,
    @Masked String password,
    String tenantId,
    @Nullable String role) {
  public RegisterCommand(String username, String email, String password, String tenantId) {
    this(username, email, password, tenantId, null);
  }
}
