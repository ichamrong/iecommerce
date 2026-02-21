package com.chamrong.iecommerce.auth.api;

import com.chamrong.iecommerce.auth.application.command.DisableUserHandler;
import com.chamrong.iecommerce.auth.application.query.UserQueryHandler;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User management endpoints — requires a valid JWT. Operations are restricted by specific
 * permissions rather than coarse roles.
 */
@Tag(name = "Users", description = "User management — requires JWT and fine-grained permissions")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserQueryHandler userQueryHandler;
  private final DisableUserHandler disableUserHandler;

  public UserController(UserQueryHandler userQueryHandler, DisableUserHandler disableUserHandler) {
    this.userQueryHandler = userQueryHandler;
    this.disableUserHandler = disableUserHandler;
  }

  /**
   * List all users in the current tenant.
   *
   * <p>GET /api/v1/users — requires {@code user:read}
   */
  @Operation(
      summary = "List all users",
      description =
          "Returns a paginated list of users in the current tenant. Requires `user:read`"
              + " permission.")
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
      description =
          "Fetch a single user by their local database ID. Requires `user:read` permission.")
  @GetMapping("/{id}")
  @PreAuthorize(Permissions.HAS_USER_READ)
  public ResponseEntity<User> getUser(@PathVariable Long id) {
    return userQueryHandler
        .findUserById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Disable a user account (soft-disable, not delete).
   *
   * <p>PATCH /api/v1/users/{id}/disable — requires {@code user:disable}
   */
  @Operation(
      summary = "Disable a user",
      description =
          "Soft-disables a user account. The user cannot login but is not deleted. Requires"
              + " `user:disable` permission.")
  @PatchMapping("/{id}/disable")
  @PreAuthorize(Permissions.HAS_USER_DISABLE)
  public ResponseEntity<Void> disableUser(@PathVariable Long id) {
    disableUserHandler.handle(id);
    return ResponseEntity.noContent().build();
  }
}
