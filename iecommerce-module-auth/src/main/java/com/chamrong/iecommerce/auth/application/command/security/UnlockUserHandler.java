package com.chamrong.iecommerce.auth.application.command.security;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * Manually unlocks a user account.
 *
 * <p>This command performs two vital actions:
 *
 * <ol>
 *   <li>Clears the local {@code LoginLockStore} (removes the progressive delay window).
 *   <li>Clears the Keycloak brute-force detection record for the user.
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnlockUserHandler {

  private final IdentityService identityService;
  private final LoginLockStore lockStore;

  /**
   * Unlocks the user. Required roles: ADMIN or TENANT_ADMIN.
   *
   * @param username the login name of the user to unlock
   * @param tenantId the tenant owning the user
   */
  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TENANT_ADMIN')")
  public void handle(final String username, final String tenantId) {
    log.info("Manually unlocking account for user='{}' in tenant='{}'", username, tenantId);

    // 1. Clear local lock
    lockStore.clear(username, tenantId);

    // 2. Clear Keycloak lock
    String keycloakId = identityService.lookupId(username);
    identityService.unlockUser(keycloakId);

    log.info("Account successfully unlocked for user='{}'", username);
  }
}
