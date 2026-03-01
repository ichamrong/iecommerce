package com.chamrong.iecommerce.auth.infrastructure.event;

import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.command.user.RegisterUserHandler;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.event.StaffAccountCreatedEvent;
import com.chamrong.iecommerce.auth.domain.event.StaffTenantsUpdatedEvent;
import com.chamrong.iecommerce.auth.domain.ports.PermissionRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.auth.infrastructure.identity.KeycloakProperties;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Listens for staff-related events and performs necessary auth-level actions via Keycloak. */
@Component
@Slf4j
public class StaffEventListener {

  private static final String SYSTEM_TENANT = "SYSTEM";

  private final UserRepositoryPort userRepository;
  private final RoleRepositoryPort roleRepository;
  private final PermissionRepositoryPort permissionRepository;
  private final RegisterUserHandler registerUserHandler;
  private final KeycloakProperties keycloakProperties;

  public StaffEventListener(
      UserRepositoryPort userRepository,
      RoleRepositoryPort roleRepository,
      PermissionRepositoryPort permissionRepository,
      RegisterUserHandler registerUserHandler,
      KeycloakProperties keycloakProperties) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
    this.registerUserHandler = registerUserHandler;
    this.keycloakProperties = keycloakProperties;
  }

  /**
   * Creates a User account in the Keycloak SYSTEM tenant when a staff profile is created.
   *
   * <p>Assigns {@code ROLE_PLATFORM_STAFF} and required permissions.
   */
  @EventListener
  @Transactional
  public void handleStaffAccountCreated(StaffAccountCreatedEvent event) {
    log.info("Handling staff account creation for user: {}", event.username());

    if (userRepository.findByUsernameAndTenantId(event.username(), SYSTEM_TENANT).isPresent()) {
      log.warn("User {} already exists in SYSTEM tenant. Skipping creation.", event.username());
      return;
    }

    // 1. Ensure ROLE_PLATFORM_STAFF exists with profile:read locally
    Permission profileRead =
        permissionRepository
            .findByName(Permissions.PROFILE_READ)
            .orElseGet(() -> permissionRepository.save(new Permission(Permissions.PROFILE_READ)));

    if (roleRepository.findByName(Role.ROLE_PLATFORM_STAFF).isEmpty()) {
      Role r = new Role(Role.ROLE_PLATFORM_STAFF);
      r.describe("Platform staff — manages assigned tenant stores");
      r.assignTo(SYSTEM_TENANT);
      r.setPermissions(Set.of(profileRead));
      roleRepository.save(r);
    }

    // 2. Register the user in Keycloak and local database
    RegisterCommand cmd =
        new RegisterCommand(
            event.username(),
            event.email(),
            event.temporaryPassword(),
            SYSTEM_TENANT,
            Role.ROLE_PLATFORM_STAFF);

    registerUserHandler.handle(cmd);

    log.info("Successfully created platform staff user in Keycloak: {}", event.username());
  }

  /** Updates Keycloak user attributes to reflect their assigned tenants. */
  @EventListener
  public void handleStaffTenantsUpdated(StaffTenantsUpdatedEvent event) {
    log.info(
        "Syncing {} tenants for staff profile {} to Keycloak",
        event.tenantCodes().size(),
        event.username());

    var userOpt = userRepository.findByUsernameAndTenantId(event.username(), SYSTEM_TENANT);
    if (userOpt.isEmpty()) {
      log.warn("Local user account missing for {}", event.username());
      return;
    }

    String keycloakId = userOpt.get().getKeycloakId();

    try (Keycloak keycloak =
        KeycloakBuilder.builder()
            .serverUrl(keycloakProperties.getServerUrl())
            .realm("master")
            .clientId(keycloakProperties.getAdmin().getClientId())
            .username(keycloakProperties.getAdmin().getUsername())
            .password(keycloakProperties.getAdmin().getPassword())
            .build()) {

      var userResource = keycloak.realm(keycloakProperties.getRealm()).users().get(keycloakId);
      UserRepresentation userRep = userResource.toRepresentation();

      // Retrieve existing attributes, overwrite "tenantId" array
      Map<String, List<String>> attributes = userRep.getAttributes();
      if (attributes == null) {
        attributes = new java.util.HashMap<>();
      }

      // Merge the global SYSTEM scope with explicitly allowed subsets
      List<String> combinedTenants = new java.util.ArrayList<>();
      combinedTenants.add(SYSTEM_TENANT);
      combinedTenants.addAll(event.tenantCodes());

      attributes.put("tenantId", combinedTenants);
      userRep.setAttributes(attributes);

      userResource.update(userRep);
      log.info("Successfully updated token scopes for {}", event.username());

    } catch (Exception e) {
      log.error(
          "Failed to update staff assignments in Keycloak for {}: {}",
          event.username(),
          e.getMessage());
    }
  }
}
