package com.chamrong.iecommerce.auth.infrastructure.identity;

import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "iecommerce.init.keycloak",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class KeycloakInitializer implements CommandLineRunner {
  private final KeycloakProperties properties;
  private final SuperAdminProperties superAdminProperties;
  private final UserRepositoryPort userRepository;
  private final RoleRepositoryPort roleRepository;
  private final Keycloak keycloak;

  @Override
  public void run(String... args) {
    log.info("Starting Keycloak Auto-Configuration at {}", properties.getServerUrl());
    try {
      createRealmIfNotExists(keycloak);
      createClientsIfNotExists(keycloak);
      createSystemRoles(keycloak);
      createSuperAdminUser(keycloak);

      log.info("Keycloak Auto-Configuration Complete.");
    } catch (Exception e) {
      log.error("Failed to initialize Keycloak: {}", e.getMessage(), e);
    }
  }

  private void createRealmIfNotExists(Keycloak keycloak) {
    boolean realmExists =
        keycloak.realms().findAll().stream()
            .anyMatch(r -> r.getRealm().equals(properties.getRealm()));

    if (!realmExists) {
      log.info("Creating Keycloak Realm '{}'...", properties.getRealm());
      RealmRepresentation realm = new RealmRepresentation();
      realm.setRealm(properties.getRealm());
      realm.setEnabled(true);
      realm.setRegistrationAllowed(true); // Important for self-service tenant registration
      keycloak.realms().create(realm);
      log.info("Realm '{}' created successfully.", properties.getRealm());
    } else {
      log.info("Keycloak Realm '{}' already exists.", properties.getRealm());
    }
  }

  private void createClientsIfNotExists(Keycloak keycloak) {
    RealmResource realmResource = keycloak.realm(properties.getRealm());
    List<ClientRepresentation> clients = realmResource.clients().findAll();

    createClient(realmResource, clients, properties.getClients().getWeb(), true);
    createClient(realmResource, clients, properties.getClients().getAdmin(), false);
  }

  private void createClient(
      RealmResource realmResource,
      List<ClientRepresentation> existingClients,
      String clientId,
      boolean publicClient) {
    boolean exists =
        existingClients.stream()
            .anyMatch(c -> clientId != null && clientId.equals(c.getClientId()));
    if (!exists && clientId != null) {
      log.info("Creating client '{}'...", clientId);
      ClientRepresentation client = new ClientRepresentation();
      client.setClientId(clientId);
      client.setEnabled(true);
      client.setPublicClient(publicClient);
      client.setDirectAccessGrantsEnabled(true);
      client.setStandardFlowEnabled(true);
      client.setImplicitFlowEnabled(true);
      client.setRedirectUris(List.of("*"));
      client.setWebOrigins(List.of("*"));

      realmResource.clients().create(client);
      log.info("Client '{}' created successfully.", clientId);
    }
  }

  private void createSystemRoles(Keycloak keycloak) {
    RealmResource realmResource = keycloak.realm(properties.getRealm());
    List<RoleRepresentation> roles = realmResource.roles().list();

    List<String> systemRoles =
        List.of(
            "ROLE_PLATFORM_ADMIN",
            "ROLE_PLATFORM_STAFF",
            "ROLE_ACCOUNTING",
            "ROLE_MODERATOR",
            "ROLE_SUPPORT",
            "ROLE_PLAN_MANAGER",
            "ROLE_SYSTEM_STATUS",
            "ROLE_TENANT_ADMIN",
            "ROLE_CUSTOMER");

    for (String roleName : systemRoles) {
      boolean exists = roles.stream().anyMatch(r -> roleName.equals(r.getName()));
      if (!exists) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        realmResource.roles().create(role);
        log.info("Created Keycloak role: {}", roleName);
      }
    }
  }

  private void createSuperAdminUser(Keycloak keycloak) {
    final String bootstrapUsername =
        superAdminProperties.getUsername() != null ? superAdminProperties.getUsername() : "admin";

    RealmResource realmResource = keycloak.realm(properties.getRealm());
    List<UserRepresentation> users =
        realmResource.users().searchByUsername(bootstrapUsername, true);

    if (users.isEmpty()) {
      log.info("Creating default '{}' user in Keycloak...", bootstrapUsername);
      UserRepresentation user = new UserRepresentation();
      user.setUsername(bootstrapUsername);
      user.setEnabled(true);
      user.setEmailVerified(true);
      user.setFirstName("System");
      user.setLastName("Admin");
      user.setRequiredActions(List.of("UPDATE_PASSWORD")); // Forces password reset on first login

      CredentialRepresentation credential = new CredentialRepresentation();
      credential.setType(CredentialRepresentation.PASSWORD);
      final String bootstrapPassword =
          superAdminProperties.getPassword() != null
              ? superAdminProperties.getPassword()
              : bootstrapUsername;
      credential.setValue(bootstrapPassword);
      credential.setTemporary(true);

      user.setCredentials(List.of(credential));

      try (jakarta.ws.rs.core.Response response = realmResource.users().create(user)) {
        if (response.getStatus() == 201) {
          String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
          RoleRepresentation adminRole =
              realmResource.roles().get("ROLE_PLATFORM_ADMIN").toRepresentation();
          realmResource.users().get(userId).roles().realmLevel().add(List.of(adminRole));
          log.info("Super admin created successfully with ID: {}", userId);

          final String email =
              superAdminProperties.getEmail() != null
                  ? superAdminProperties.getEmail()
                  : bootstrapUsername + "@platform.com";

          syncSuperAdminToDatabase(userId, bootstrapUsername, email);
        } else {
          log.error("Failed to create super admin user. HTTP Status: {}", response.getStatus());
        }
      }
    } else {
      log.info("Super admin user already exists in Keycloak.");
      UserRepresentation existingUser = users.getFirst();
      String email =
          existingUser.getEmail() != null
              ? existingUser.getEmail()
              : existingUser.getUsername() + "@platform.com";
      syncSuperAdminToDatabase(existingUser.getId(), existingUser.getUsername(), email);
    }
  }

  private void syncSuperAdminToDatabase(String keycloakId, String username, String email) {
    TenantContext.setCurrentTenant("SYSTEM");
    try {
      if (userRepository.findByKeycloakId(keycloakId).isEmpty()) {
        log.info("Syncing super admin from Keycloak to local database...");
        com.chamrong.iecommerce.auth.domain.Role adminRole =
            roleRepository
                .findByName(com.chamrong.iecommerce.auth.domain.Role.ROLE_PLATFORM_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_PLATFORM_ADMIN not found"));

        com.chamrong.iecommerce.auth.domain.User localUser =
            new com.chamrong.iecommerce.auth.domain.User("SYSTEM", username, email);
        localUser.linkKeycloak(keycloakId);
        localUser.activate();
        localUser.addRole(adminRole);

        userRepository.save(localUser);
      }
    } finally {
      TenantContext.clear();
    }
  }
}
