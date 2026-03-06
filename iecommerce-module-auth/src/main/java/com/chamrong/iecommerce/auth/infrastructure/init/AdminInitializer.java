package com.chamrong.iecommerce.auth.infrastructure.init;

import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.ports.PermissionRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Initializes the system with required permissions, roles, and a default Platform Admin user. */
@Component
@Order(0)
@ConditionalOnProperty(
    prefix = "iecommerce.init.admin",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AdminInitializer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);
  private static final String SYSTEM_TENANT = "SYSTEM";

  private final PermissionRepositoryPort permissionRepository;
  private final RoleRepositoryPort roleRepository;

  public AdminInitializer(
      PermissionRepositoryPort permissionRepository, RoleRepositoryPort roleRepository) {
    this.permissionRepository = permissionRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  @Transactional
  public void run(String... args) {
    try {
      TenantContext.setCurrentTenant(SYSTEM_TENANT);
      log.info("Starting system administrative initialization (Roles & Permissions)...");

      // 1. Ensure Permissions exist
      Permission userRead = ensurePermission(Permissions.USER_READ);
      Permission userCreate = ensurePermission(Permissions.USER_CREATE);
      Permission userDisable = ensurePermission(Permissions.USER_DISABLE);
      Permission profileRead = ensurePermission(Permissions.PROFILE_READ);
      Permission tenantCreate = ensurePermission(Permissions.TENANT_CREATE);
      Permission staffManage = ensurePermission(Permissions.STAFF_MANAGE);

      // 2. Ensure Roles exist and assign permissions
      ensureRole(
          Role.ROLE_PLATFORM_ADMIN,
          "Super administrator with full platform access",
          Set.of(userRead, userCreate, userDisable, profileRead, tenantCreate, staffManage));
      ensureRole(
          Role.ROLE_ACCOUNTING, "Financial and accounting management access", Set.of(profileRead));
      ensureRole(
          Role.ROLE_MODERATOR, "Content and user moderation access", Set.of(userRead, profileRead));
      ensureRole(
          Role.ROLE_SYSTEM_STATUS,
          "System health and status monitoring access",
          Set.of(profileRead));
      ensureRole(
          Role.ROLE_TENANT_ADMIN, "Tenant owner — manages their own store", Set.of(profileRead));
      ensureRole(
          Role.ROLE_PLATFORM_STAFF,
          "Platform staff — manages assigned tenant stores",
          Set.of(profileRead));
      ensureRole(Role.ROLE_CUSTOMER, "Standard shopper profile", Set.of(profileRead));

      log.info("System administrative initialization completed.");
    } finally {
      TenantContext.clear();
    }
  }

  private Permission ensurePermission(String name) {
    return permissionRepository
        .findByName(name)
        .orElseGet(
            () -> {
              log.info("Creating permission: {}", name);
              return permissionRepository.save(new Permission(name));
            });
  }

  private void ensureRole(String roleName, String description, Set<Permission> permissions) {
    Role role =
        roleRepository
            .findByName(roleName)
            .orElseGet(
                () -> {
                  log.info("Creating role: {}", roleName);
                  Role r = new Role(roleName);
                  r.describe(description);
                  r.assignTo(SYSTEM_TENANT);
                  return r;
                });
    role.getPermissions().clear();
    role.getPermissions().addAll(permissions);
    roleRepository.save(role);
  }
}
