package com.chamrong.iecommerce.auth.application.command.user;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Enables TOTP-based two-factor authentication for the currently authenticated user.
 *
 * <h3>Flow</h3>
 *
 * <ol>
 *   <li>This handler adds the {@code CONFIGURE_TOTP} required action to the user in Keycloak.
 *   <li>Keycloak automatically sends an email with a link to set up an authenticator app, OR the
 *       user is prompted to scan a QR code on their next login.
 *   <li>After scanning, the user's account requires OTP on every subsequent login.
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Enable2FAHandler {

  private final IdentityService identityService;

  /** Enables 2FA for the currently authenticated user. */
  public void handle() {
    var username = SecurityContextHolder.getContext().getAuthentication().getName();
    var keycloakId = identityService.lookupId(username);
    identityService.enableTotpForUser(keycloakId);
    log.info("2FA CONFIGURE_TOTP action added for user '{}'.", username);
  }
}
