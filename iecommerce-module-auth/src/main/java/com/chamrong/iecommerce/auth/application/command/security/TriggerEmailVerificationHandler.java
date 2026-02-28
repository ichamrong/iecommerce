package com.chamrong.iecommerce.auth.application.command.security;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manually triggers a verification email for a user.
 *
 * <p>Useful for self-service "Resend verification email" or for admins to prompt users to verify
 * their identity.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerEmailVerificationHandler {

  private final IdentityService identityService;

  /**
   * Sends the verification email.
   *
   * @param username the user to verify
   */
  public void handle(final String username) {
    log.info("Triggering verification email for user='{}'", username);
    String keycloakId = identityService.lookupId(username);
    identityService.sendVerificationEmail(keycloakId);
    log.info("Verification email sent for user='{}'", username);
  }
}
