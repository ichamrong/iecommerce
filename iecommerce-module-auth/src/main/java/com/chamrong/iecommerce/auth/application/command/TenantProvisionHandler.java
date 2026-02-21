package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.application.dto.TenantResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.PermissionRepository;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.RoleRepository;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantRepository;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles platform-admin tenant provisioning.
 *
 * <p>Creates an ACTIVE tenant with admin-specified plan; generates a temp password for the owner
 * via Keycloak.
 */
@Component
public class TenantProvisionHandler {

  private final TenantRepository tenantRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final RegisterUserHandler registerUserHandler;

  public TenantProvisionHandler(
      TenantRepository tenantRepository,
      RoleRepository roleRepository,
      PermissionRepository permissionRepository,
      RegisterUserHandler registerUserHandler) {
    this.tenantRepository = tenantRepository;
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
    this.registerUserHandler = registerUserHandler;
  }

  @Transactional
  public TenantResponse handle(TenantProvisionCommand cmd) {
    if (tenantRepository.existsByCode(cmd.tenantCode())) {
      throw new DuplicateUserException("Tenant code already exists: " + cmd.tenantCode());
    }

    // 1. Create tenant — starts ACTIVE (admin has vetted it)
    Tenant tenant = new Tenant();
    tenant.setCode(cmd.tenantCode());
    tenant.setName(cmd.shopName());
    tenant.setPlan(cmd.plan());
    tenant.setStatus(TenantStatus.ACTIVE);
    tenantRepository.save(tenant);

    TenantContext.setCurrentTenant(cmd.tenantCode());
    String tempPassword;
    try {
      // 2. Ensure ROLE_TENANT_ADMIN exists locally
      ensureTenantAdminRole(cmd.tenantCode());

      // 3. Create owner with temp password in Keycloak
      tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
      String ownerUsername = cmd.ownerEmail().split("@")[0];

      RegisterCommand regCmd =
          new RegisterCommand(
              ownerUsername,
              cmd.ownerEmail(),
              tempPassword,
              cmd.tenantCode(),
              Role.ROLE_TENANT_ADMIN);
      registerUserHandler.handle(regCmd);
    } finally {
      TenantContext.clear();
    }

    return new TenantResponse(
        cmd.tenantCode(),
        cmd.shopName(),
        cmd.plan(),
        TenantStatus.ACTIVE,
        cmd.ownerEmail(),
        tempPassword);
  }

  private Role ensureTenantAdminRole(String tenantCode) {
    Permission profileRead =
        permissionRepository
            .findByName(Permissions.PROFILE_READ)
            .orElseGet(() -> permissionRepository.save(new Permission(Permissions.PROFILE_READ)));

    Role role =
        roleRepository
            .findByName(Role.ROLE_TENANT_ADMIN)
            .orElseGet(
                () -> {
                  Role r = new Role(Role.ROLE_TENANT_ADMIN);
                  r.setDescription("Tenant owner — manages their own store");
                  r.setTenantId(tenantCode);
                  return r;
                });
    role.setPermissions(Set.of(profileRead));
    return roleRepository.save(role);
  }
}
