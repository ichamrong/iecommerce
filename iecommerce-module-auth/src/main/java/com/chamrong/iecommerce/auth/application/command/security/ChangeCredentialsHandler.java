package com.chamrong.iecommerce.auth.application.command.security;

import com.chamrong.iecommerce.auth.application.command.ChangeCredentialsCommand;
import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.auth.LoginUserHandler;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.application.exception.InvalidPasswordException;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.UserAccountState;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Handles a first-login style credentials update where the user is required to change both their
 * username and password.
 *
 * <p>The handler:
 *
 * <ol>
 *   <li>Verifies the current password via an internal login.
 *   <li>Validates uniqueness of the new username within the tenant.
 *   <li>Updates username and password in the Identity Provider.
 *   <li>Mirrors the changes to the local {@code auth_user} table, including audit fields.
 *   <li>Transitions {@link com.chamrong.iecommerce.auth.domain.User} from {@link
 *       UserAccountState#PENDING} to {@link UserAccountState#ACTIVE} on success.
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeCredentialsHandler {

  private static final int MIN_PASSWORD_LENGTH = 8;

  private final IdentityService identityService;
  private final LoginUserHandler loginUserHandler;
  private final UserRepositoryPort userRepository;

  /**
   * Verifies the current password, then updates both username and password.
   *
   * @param cmd command containing current password, new username, and new password
   */
  public void handle(ChangeCredentialsCommand cmd) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new BadCredentialsException("No authenticated user in security context");
    }

    var currentUsername = authentication.getName();
    var tenantId = TenantContext.requireTenantId();

    // 1. Verify current password by attempting an internal login
    try {
      loginUserHandler.handle(new LoginCommand(currentUsername, cmd.currentPassword(), tenantId));
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid current password");
    }

    // 2. Validate password strength
    if (cmd.newPassword().length() < MIN_PASSWORD_LENGTH) {
      throw new InvalidPasswordException(
          "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
    }

    // 3. Load local user
    var user =
        userRepository
            .findByUsernameAndTenantId(currentUsername, tenantId)
            .orElseThrow(() -> new BadCredentialsException("User not found for current session"));

    // 4. Validate and apply new username
    var trimmedUsername = cmd.newUsername().trim().toLowerCase();
    if (!trimmedUsername.equals(currentUsername.toLowerCase())) {
      userRepository
          .findByUsernameAndTenantId(trimmedUsername, tenantId)
          .ifPresent(
              existing -> {
                throw new DuplicateUserException(
                    "Username already exists in this tenant: " + trimmedUsername);
              });
      user.changeUsername(trimmedUsername);
    }

    // 5. Update credentials in Identity Provider
    var keycloakId = identityService.lookupId(currentUsername);
    if (!trimmedUsername.equals(currentUsername.toLowerCase())) {
      identityService.updateUsername(keycloakId, trimmedUsername);
    }
    identityService.updatePassword(keycloakId, cmd.newPassword());

    // 6. Mirror audit fields locally
    user.markPasswordChanged(Instant.now());
    if (user.getAccountState() == UserAccountState.PENDING) {
      user.activate();
    }

    userRepository.save(user);

    log.info(
        "Credentials updated successfully for user '{}' in tenant '{}'",
        user.getUsername(),
        tenantId);
  }
}
