package com.chamrong.iecommerce.auth.application.command;

/**
 * Command to register a new user within a specific tenant.
 *
 * @param username unique login name
 * @param email unique email address
 * @param password raw (unhashed) password
 * @param tenantId the tenant this user belongs to
 */
public record RegisterCommand(
    String username, String email, String password, String tenantId, String role) {
  public RegisterCommand(String username, String email, String password, String tenantId) {
    this(username, email, password, tenantId, null);
  }
}
