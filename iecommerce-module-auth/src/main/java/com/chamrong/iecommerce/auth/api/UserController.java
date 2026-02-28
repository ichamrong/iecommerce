package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.command.user.AdminCreateUserCommand;
import com.chamrong.iecommerce.auth.application.command.user.AdminCreateUserHandler;
import com.chamrong.iecommerce.auth.application.command.user.Disable2FAHandler;
import com.chamrong.iecommerce.auth.application.command.user.DisableUserHandler;
import com.chamrong.iecommerce.auth.application.command.user.Enable2FAHandler;
import com.chamrong.iecommerce.auth.application.query.UserQueryHandler;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User management endpoints — requires a valid JWT. Operations are restricted by fine-grained
 * permissions rather than coarse roles (OWASP A01 — Broken Access Control).
 */
@Tag(name = "Users", description = "User management — requires JWT and fine-grained permissions")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserQueryHandler userQueryHandler;
  private final DisableUserHandler disableUserHandler;
  private final AdminCreateUserHandler adminCreateUserHandler;
  private final Enable2FAHandler enable2FAHandler;
  private final Disable2FAHandler disable2FAHandler;

  public UserController(
      UserQueryHandler userQueryHandler,
      DisableUserHandler disableUserHandler,
      AdminCreateUserHandler adminCreateUserHandler,
      Enable2FAHandler enable2FAHandler,
      Disable2FAHandler disable2FAHandler) {
    this.userQueryHandler = userQueryHandler;
    this.disableUserHandler = disableUserHandler;
    this.adminCreateUserHandler = adminCreateUserHandler;
    this.enable2FAHandler = enable2FAHandler;
    this.disable2FAHandler = disable2FAHandler;
  }

  /**
   * List all users in the current tenant.
   *
   * <p>GET /api/v1/users — requires {@code user:read}
   */
  @Operation(
      summary = "List all users",
      description =
          "Returns a paginated list of users in the current tenant. Requires `user:read`.")
  @GetMapping
  @PreAuthorize(Permissions.HAS_USER_READ)
  public ResponseEntity<Page<User>> listUsers(@PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(userQueryHandler.findAllUsers(pageable));
  }

  /**
   * Fetch a user by id.
   *
   * <p>GET /api/v1/users/{id} — requires {@code user:read}
   */
  @Operation(
      summary = "Get user by ID",
      description = "Fetch a single user by ID. Requires `user:read`.")
  @GetMapping("/{id}")
  @PreAuthorize(Permissions.HAS_USER_READ)
  public ResponseEntity<User> getUser(@PathVariable Long id) {
    return userQueryHandler
        .findUserById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Admin creates a user account with a temporary password.
   *
   * <p>POST /api/v1/users — requires {@code staff:manage}.
   *
   * <p>Keycloak sets the {@code UPDATE_PASSWORD} required action, forcing the user to change their
   * password on first login. An invitation email is sent by Keycloak automatically.
   */
  @Operation(
      summary = "Admin: Create user",
      description =
          "Creates a user with a temporary password and sends an invitation email via Keycloak. "
              + "The user must set a new password on first login. Requires `staff:manage`.")
  @PostMapping
  @PreAuthorize(Permissions.HAS_STAFF_MANAGE)
  public ResponseEntity<Void> adminCreateUser(@Valid @RequestBody AdminCreateUserCommand cmd) {
    adminCreateUserHandler.handle(cmd);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /**
   * Disable a user account (soft-disable, not delete).
   *
   * <p>PATCH /api/v1/users/{id}/disable — requires {@code user:disable}
   */
  @Operation(
      summary = "Disable a user",
      description =
          "Soft-disables a user. The user cannot login but is not deleted. Requires"
              + " `user:disable`.")
  @PatchMapping("/{id}/disable")
  @PreAuthorize(Permissions.HAS_USER_DISABLE)
  public ResponseEntity<Void> disableUser(@PathVariable Long id) {
    disableUserHandler.handle(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Enable TOTP 2FA for the currently authenticated user.
   *
   * <p>POST /api/v1/users/me/2fa — requires a valid JWT.
   *
   * <p>Adds the {@code CONFIGURE_TOTP} required action to the user. Keycloak prompts for QR code
   * scan on next login.
   */
  @Operation(
      summary = "Enable 2FA (TOTP)",
      description =
          "Adds CONFIGURE_TOTP action to the user in Keycloak. "
              + "The user will be prompted to set up an authenticator app on next login.")
  @PostMapping("/me/2fa")
  public ResponseEntity<Void> enable2FA() {
    enable2FAHandler.handle();
    return ResponseEntity.ok().build();
  }

  /**
   * Disable TOTP 2FA for the currently authenticated user.
   *
   * <p>DELETE /api/v1/users/me/2fa — requires a valid JWT.
   */
  @Operation(
      summary = "Disable 2FA (TOTP)",
      description =
          "Removes OTP credentials and the CONFIGURE_TOTP action from the user's Keycloak account.")
  @DeleteMapping("/me/2fa")
  public ResponseEntity<Void> disable2FA() {
    disable2FAHandler.handle();
    return ResponseEntity.noContent().build();
  }
}
