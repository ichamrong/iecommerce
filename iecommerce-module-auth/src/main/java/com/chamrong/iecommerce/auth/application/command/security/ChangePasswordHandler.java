package com.chamrong.iecommerce.auth.application.command.security;

import com.chamrong.iecommerce.auth.application.command.ChangePasswordCommand;
import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.auth.LoginUserHandler;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.common.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Changes the authenticated user's own password after verifying the current one.
 *
 * <p>Used both for voluntary password changes and to fulfil a forced first-login reset (clears
 * {@code UPDATE_PASSWORD} required action on the Keycloak side).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordHandler {

  private final IdentityService identityService;
  private final LoginUserHandler loginUserHandler;

  /** Verifies the current password then updates it to the new one. */
  public void handle(ChangePasswordCommand cmd) {
    var username = SecurityContextHolder.getContext().getAuthentication().getName();
    var tenantId = TenantContext.requireTenantId();

    // 1. Verify current password by attempting an internal login
    try {
      loginUserHandler.handle(new LoginCommand(username, cmd.currentPassword(), tenantId));
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid current password");
    }

    // 2. Update password — sets temporary=false, clearing UPDATE_PASSWORD action in Keycloak
    var keycloakId = identityService.lookupId(username);
    identityService.updatePassword(keycloakId, cmd.newPassword());
    log.info("Password changed successfully for user: {}", username);
  }
}
