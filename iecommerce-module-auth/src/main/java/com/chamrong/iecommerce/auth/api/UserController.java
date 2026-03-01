package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.command.security.Disable2FAHandler;
import com.chamrong.iecommerce.auth.application.command.security.Enable2FAHandler;
import com.chamrong.iecommerce.auth.application.command.security.ResetUserPasswordHandler;
import com.chamrong.iecommerce.auth.application.command.security.RevokeAllSessionsHandler;
import com.chamrong.iecommerce.auth.application.command.security.RevokeSessionHandler;
import com.chamrong.iecommerce.auth.application.command.security.TriggerEmailVerificationHandler;
import com.chamrong.iecommerce.auth.application.command.security.UnlockUserHandler;
import com.chamrong.iecommerce.auth.application.command.user.AdminCreateUserCommand;
import com.chamrong.iecommerce.auth.application.command.user.AdminCreateUserHandler;
import com.chamrong.iecommerce.auth.application.command.user.DisableUserHandler;
import com.chamrong.iecommerce.auth.application.query.UserQueryHandler;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.security.TenantGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
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
  private final UnlockUserHandler unlockUserHandler;
  private final TriggerEmailVerificationHandler triggerEmailVerificationHandler;
  private final RevokeSessionHandler revokeSessionHandler;
  private final RevokeAllSessionsHandler revokeAllSessionsHandler;
  private final ResetUserPasswordHandler resetUserPasswordHandler;

  public UserController(
      UserQueryHandler userQueryHandler,
      DisableUserHandler disableUserHandler,
      AdminCreateUserHandler adminCreateUserHandler,
      Enable2FAHandler enable2FAHandler,
      Disable2FAHandler disable2FAHandler,
      UnlockUserHandler unlockUserHandler,
      TriggerEmailVerificationHandler triggerEmailVerificationHandler,
      RevokeSessionHandler revokeSessionHandler,
      RevokeAllSessionsHandler revokeAllSessionsHandler,
      ResetUserPasswordHandler resetUserPasswordHandler) {
    this.userQueryHandler = userQueryHandler;
    this.disableUserHandler = disableUserHandler;
    this.adminCreateUserHandler = adminCreateUserHandler;
    this.enable2FAHandler = enable2FAHandler;
    this.disable2FAHandler = disable2FAHandler;
    this.unlockUserHandler = unlockUserHandler;
    this.triggerEmailVerificationHandler = triggerEmailVerificationHandler;
    this.revokeSessionHandler = revokeSessionHandler;
    this.revokeAllSessionsHandler = revokeAllSessionsHandler;
    this.resetUserPasswordHandler = resetUserPasswordHandler;
  }

  /**
   * List all users in the current tenant (cursor-paginated).
   *
   * <p>GET /api/v1/users — requires {@code user:read}. No offset; use cursor and limit.
   */
  @Operation(
      summary = "List all users",
      description =
          "Returns a cursor-paginated list of users in the current tenant. Requires `user:read`.")
  @GetMapping
  @PreAuthorize(Permissions.HAS_USER_READ)
  public ResponseEntity<CursorPageResponse<User>> listUsers(
      @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int limit) {
    String tenantId = TenantContext.getCurrentTenant();
    int clampedLimit = Math.min(100, Math.max(1, limit));
    return ResponseEntity.ok(
        userQueryHandler.listUsers(tenantId, cursor, clampedLimit, java.util.Map.of()));
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
    String tenantId = TenantContext.getCurrentTenant();
    return userQueryHandler
        .findUserById(id)
        .map(
            user -> {
              TenantGuard.requireSameTenant(user.getTenantId(), tenantId);
              return ResponseEntity.ok(user);
            })
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

  /**
   * Unlock a user account manually (clears progressive lock and Keycloak brute-force lock).
   *
   * <p>POST /api/v1/users/{username}/unlock — requires {@code staff:manage}.
   */
  @Operation(
      summary = "Admin: Unlock user",
      description =
          "Manually clears both local progressive delay lock and Keycloak brute-force lock. "
              + "Requires `staff:manage`.")
  @PostMapping("/{username}/unlock")
  @PreAuthorize(Permissions.HAS_STAFF_MANAGE)
  public ResponseEntity<Void> unlockUser(
      @PathVariable String username,
      @RequestBody(required = false)
          String
              tenantId) { // tenantId usually from context, added here for explicit admin override
    // if needed later
    // In a real scenario, you might extract username from path and tenantId from context or body.
    // For simplicity, assuming username here is enough for the handler if it uses TenantContext
    // internally.
    // Let's adjust to pass null for tenantId if the handler derives it, or modify handler to take
    // just username.
    // Looking at UnlockUserHandler, it expects username and tenantId.
    // We should get tenantId from TenantContext in the controller or let handler do it.
    // Let's pass null for tenantId parameter here and let handler use TenantContext if needed,
    // or better yet, extract it implicitly.
    // Actually, UnlockUserHandler signature: handle(final String username, final String tenantId).
    // Let's just pass null for now or retrieve it from context.
    String currentTenantId = com.chamrong.iecommerce.common.TenantContext.requireTenantId();
    unlockUserHandler.handle(username, currentTenantId);
    return ResponseEntity.ok().build();
  }

  /**
   * Triggers a verification email for the user.
   *
   * <p>POST /api/v1/users/{username}/verify-email — requires {@code staff:manage}.
   */
  @Operation(
      summary = "Admin: Trigger verification email",
      description =
          "Sends an email for the user to verify their email address via Keycloak. "
              + "Requires `staff:manage`.")
  @PostMapping("/{username}/verify-email")
  @PreAuthorize(Permissions.HAS_STAFF_MANAGE)
  public ResponseEntity<Void> triggerVerificationEmail(@PathVariable String username) {
    triggerEmailVerificationHandler.handle(username);
    return ResponseEntity.ok().build();
  }

  /**
   * Revoke a specific user session.
   *
   * <p>DELETE /api/v1/users/sessions/{sessionId}
   */
  @Operation(
      summary = "Revoke single session",
      description = "Invalidates a specific Keycloak session by ID. Requires `staff:manage`.")
  @DeleteMapping("/sessions/{sessionId}")
  @PreAuthorize(Permissions.HAS_STAFF_MANAGE)
  public ResponseEntity<Void> revokeSession(
      @PathVariable String sessionId,
      @RequestBody String keycloakId) { // Need keycloakId as parameter according to handler
    revokeSessionHandler.handle(sessionId, keycloakId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Revoke all sessions for a specific user.
   *
   * <p>DELETE /api/v1/users/{username}/sessions
   */
  @Operation(
      summary = "Revoke all sessions for user",
      description =
          "Logs the user out entirely by revoking all their active Keycloak sessions. "
              + "Requires `staff:manage`.")
  @DeleteMapping("/{username}/sessions")
  @PreAuthorize(Permissions.HAS_STAFF_MANAGE)
  public ResponseEntity<Void> revokeAllSessions(
      @PathVariable String username,
      @RequestBody
          String keycloakId) { // Changed to take keycloakId as body since path has username
    // Or normally we'd look up keycloakId using identityService based on username
    revokeAllSessionsHandler.handle(keycloakId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Admin resets a user's password to a temporary forced one.
   *
   * <p>POST /api/v1/users/{username}/reset-password
   */
  @Operation(
      summary = "Admin: Reset password",
      description =
          "Forces a password reset. Sets a temporary password and forces change on next login. "
              + "Requires `staff:manage`.")
  @PostMapping("/{username}/reset-password")
  @PreAuthorize(Permissions.HAS_STAFF_MANAGE)
  public ResponseEntity<Void> resetPassword(
      @PathVariable String username, @RequestBody String newTemporaryPassword) {
    resetUserPasswordHandler.handle(username, newTemporaryPassword);
    return ResponseEntity.ok().build();
  }
}
