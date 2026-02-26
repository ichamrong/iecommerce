package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.UserRegisteredEvent;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.RoleRepository;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.UserRepository;
import com.chamrong.iecommerce.auth.infrastructure.init.KeycloakProperties;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserHandler {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final KeycloakProperties properties;
  private final LoginUserHandler loginUserHandler;
  private final Keycloak keycloak;

  @Transactional
  public AuthResponse handle(RegisterCommand cmd) {
    if (userRepository.findByUsernameAndTenantId(cmd.username(), cmd.tenantId()).isPresent()) {
      throw new DuplicateUserException("Username already exists in this tenant: " + cmd.username());
    }
    if (userRepository.findByEmailAndTenantId(cmd.email(), cmd.tenantId()).isPresent()) {
      throw new DuplicateUserException("Email already exists in this tenant: " + cmd.email());
    }

    // OWASP ASVS 5.0 Level 1 - Requirement V6
    if (cmd.password() == null || cmd.password().length() < 8) {
      throw new com.chamrong.iecommerce.auth.application.exception.InvalidPasswordException(
          "Password must be at least 8 characters long.");
    }

    RealmResource realmResource = keycloak.realm(properties.getRealm());

    UserRepresentation userRep = new UserRepresentation();
    userRep.setUsername(cmd.username());
    userRep.setEmail(cmd.email());
    userRep.setEnabled(true);
    userRep.setEmailVerified(true);

    // Critical for multi-tenant mapping extracted by KeycloakJwtAuthenticationConverter
    userRep.setAttributes(Map.of("tenantId", List.of(cmd.tenantId())));

    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(cmd.password());
    credential.setTemporary(false);

    userRep.setCredentials(List.of(credential));

    try (var response = realmResource.users().create(userRep)) {
      if (response.getStatus() == 201 || response.getStatus() == 200) {
        var keycloakId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        // Assign required Role
        var targetRole = cmd.role() != null ? cmd.role() : Role.ROLE_CUSTOMER;
        try {
          var assignedRole = realmResource.roles().get(targetRole).toRepresentation();
          realmResource.users().get(keycloakId).roles().realmLevel().add(List.of(assignedRole));
        } catch (Exception e) {
          log.warn(
              "Could not assign {} to user {}: {}", targetRole, cmd.username(), e.getMessage());
        }

        // Save locally
        var localRole =
            roleRepository
                .findByName(targetRole)
                .orElseThrow(() -> new IllegalStateException(targetRole + " not found locally"));

        var user = new User();
        user.setUsername(cmd.username());
        user.setEmail(cmd.email());
        user.setKeycloakId(keycloakId);
        user.setTenantId(cmd.tenantId());
        user.setRoles(Set.of(localRole));
        user.setEnabled(true);

        var saved = userRepository.save(user);

        // Publish Event for Customer Module profile creation (CQRS)
        eventPublisher.publishEvent(
            new UserRegisteredEvent(
                saved.getId(), saved.getUsername(), saved.getEmail(), saved.getTenantId()));

        // Auto-login to return tokens immediately to the frontend
        return loginUserHandler.handle(
            new LoginCommand(cmd.username(), cmd.password(), cmd.tenantId()));
      } else if (response.getStatus() == 409) {
        throw new DuplicateUserException("Username or Email already exists in Keycloak.");
      } else {
        log.error("Failed to create user in Keycloak. Status: {}", response.getStatus());
        throw new RuntimeException("Failed to register user in Identity Provider.");
      }
    }
  }
}
