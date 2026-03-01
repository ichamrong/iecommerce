package com.chamrong.iecommerce.auth.application.command.user;

import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.UserAccountFactory;
import com.chamrong.iecommerce.auth.domain.event.UserRegisteredEvent;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.common.annotation.WithTenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Platform / tenant admin creates a user account with a temporary password.
 *
 * <h3>First-login flow</h3>
 *
 * <ol>
 *   <li>Admin calls {@code POST /api/v1/users} with {@link AdminCreateUserCommand}.
 *   <li>This handler creates the user in Keycloak with {@code temporary=true} credential.
 *   <li>Keycloak auto-adds {@code UPDATE_PASSWORD} required action.
 *   <li>Handler sends a Keycloak invitation email via Execute-Actions-Email.
 *   <li>User clicks the email link, sets their own password, and is then logged in normally.
 *   <li>The first POST /api/v1/auth/login response will have {@code requiresPasswordChange: true}
 *       if the user did not use the email link.
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminCreateUserHandler {

  private final UserRepositoryPort userRepository;
  private final RoleRepositoryPort roleRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final IdentityService identityService;

  @Transactional
  @WithTenantContext(tenantId = "#cmd.tenantId")
  @PreAuthorize("hasAuthority('staff:manage')")
  public void handle(AdminCreateUserCommand cmd) {
    if (userRepository.findByUsernameAndTenantId(cmd.username(), cmd.tenantId()).isPresent()) {
      throw new DuplicateUserException("Username already exists in this tenant: " + cmd.username());
    }
    if (userRepository.findByEmailAndTenantId(cmd.email(), cmd.tenantId()).isPresent()) {
      throw new DuplicateUserException("Email already exists in this tenant: " + cmd.email());
    }

    // 1. Create user in Keycloak with temporary=true password
    var registerCmd =
        new RegisterCommand(
            cmd.username(), cmd.email(), cmd.temporaryPassword(), cmd.tenantId(), cmd.role());
    String keycloakId = identityService.createUserWithTemporaryPassword(registerCmd);

    // 2. Send invitation / activation email via Keycloak Execute-Actions-Email
    identityService.sendPasswordResetEmail(keycloakId);

    // 3. Mirror to local DB
    var targetRole = cmd.role() != null ? cmd.role() : Role.ROLE_CUSTOMER;
    var localRole =
        roleRepository
            .findByName(targetRole)
            .orElseThrow(() -> new IllegalStateException(targetRole + " not found locally"));

    var user = UserAccountFactory.createAdminInvited(cmd.username(), cmd.email(), cmd.tenantId());
    user.linkKeycloak(keycloakId);
    user.addRole(localRole);

    var saved = userRepository.save(user);

    eventPublisher.publishEvent(
        new UserRegisteredEvent(
            saved.getId(), saved.getUsername(), saved.getEmail(), saved.getTenantId()));

    log.info(
        "Admin created user '{}' in tenant '{}'. Invitation email sent.",
        cmd.username(),
        cmd.tenantId());
  }
}
