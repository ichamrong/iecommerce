package com.chamrong.iecommerce.auth.application.command.user;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.command.auth.LoginUserHandler;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.application.exception.InvalidPasswordException;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.event.UserRegisteredEvent;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.common.annotation.WithTenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Self-service user registration handler.
 *
 * <p>Registers the user in Keycloak, mirrors them to the local DB, publishes a {@link
 * UserRegisteredEvent}, and auto-logs in to return tokens immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserHandler {

  private final UserRepositoryPort userRepository;
  private final RoleRepositoryPort roleRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final LoginUserHandler loginUserHandler;
  private final IdentityService identityService;

  @Transactional
  @WithTenantContext(tenantId = "#cmd.tenantId")
  public AuthResponse handle(RegisterCommand cmd) {
    if (userRepository.findByUsernameAndTenantId(cmd.username(), cmd.tenantId()).isPresent()) {
      throw new DuplicateUserException("Username already exists in this tenant: " + cmd.username());
    }
    if (userRepository.findByEmailAndTenantId(cmd.email(), cmd.tenantId()).isPresent()) {
      throw new DuplicateUserException("Email already exists in this tenant: " + cmd.email());
    }

    // OWASP ASVS 5.0 Level 1 - Requirement V6
    if (cmd.password().length() < 8) {
      throw new InvalidPasswordException("Password must be at least 8 characters long.");
    }

    // Register in IDP
    String keycloakId = identityService.registerUser(cmd);

    // Sync to local DB
    var targetRole = cmd.role() != null ? cmd.role() : Role.ROLE_CUSTOMER;
    var localRole =
        roleRepository
            .findByName(targetRole)
            .orElseThrow(() -> new IllegalStateException(targetRole + " not found locally"));

    var user = new User(cmd.tenantId(), cmd.username(), cmd.email());
    user.linkKeycloak(keycloakId);
    user.addRole(localRole);

    var saved = userRepository.save(user);

    // Publish Event for Customer Module profile creation (CQRS)
    eventPublisher.publishEvent(
        new UserRegisteredEvent(
            saved.getId(), saved.getUsername(), saved.getEmail(), saved.getTenantId()));

    // Auto-login to return tokens immediately to the frontend
    return loginUserHandler.handle(
        new LoginCommand(cmd.username(), cmd.password(), cmd.tenantId()));
  }
}
