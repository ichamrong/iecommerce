package com.chamrong.iecommerce.auth.application.command.security;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Disables TOTP-based two-factor authentication for the currently authenticated user.
 *
 * <p>Removes all OTP credentials from Keycloak and clears the {@code CONFIGURE_TOTP} required
 * action, so the user can log in again without an authenticator app.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Disable2FAHandler {

  private final IdentityService identityService;

  /** Disables 2FA for the currently authenticated user. */
  public void handle() {
    var username = SecurityContextHolder.getContext().getAuthentication().getName();
    var keycloakId = identityService.lookupId(username);
    identityService.disableTotpForUser(keycloakId);
    log.info("2FA disabled for user '{}'.", username);
  }
}
