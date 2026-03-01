package com.chamrong.iecommerce.auth.application.command.tenant;

import com.chamrong.iecommerce.auth.application.command.TenantProvisionCommand;
import com.chamrong.iecommerce.auth.application.dto.TenantResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.application.saga.TenantProvisionSaga;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantProvisioningStatus;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import com.chamrong.iecommerce.auth.domain.ports.PermissionRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import com.chamrong.iecommerce.common.annotation.WithTenantContext;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles platform-admin tenant provisioning.
 *
 * <p>Creates an ACTIVE tenant with admin-specified plan; generates a temp password for the owner
 * via Keycloak, forcing password reset on first login.
 */
@Component
@RequiredArgsConstructor
public class TenantProvisionHandler {

  private final TenantRepositoryPort tenantRepository;
  private final RoleRepositoryPort roleRepository;
  private final PermissionRepositoryPort permissionRepository;
  private final TenantProvisionSaga provisionSaga;

  @Transactional
  @WithTenantContext(tenantId = "#cmd.tenantCode")
  public TenantResponse handle(TenantProvisionCommand cmd) {
    if (tenantRepository.existsByCode(cmd.tenantCode())) {
      throw new DuplicateUserException("Tenant code already exists: " + cmd.tenantCode());
    }

    // 1. Create tenant shell
    Tenant tenant =
        Tenant.provision(
            cmd.tenantCode(),
            cmd.shopName(),
            cmd.plan(),
            TenantStatus.ACTIVE,
            TenantProvisioningStatus.INITIAL);
    Tenant savedTenant = tenantRepository.save(tenant);

    // 2. Ensure local ROLE_TENANT_ADMIN exists
    ensureTenantAdminRole(cmd.tenantCode());

    // 3. Generate temp password and delegate to Saga (temporary=true → forced reset on first login)
    String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

    provisionSaga.execute(savedTenant, cmd.ownerEmail(), tempPassword, false);

    return new TenantResponse(
        cmd.tenantCode(),
        cmd.shopName(),
        cmd.plan(),
        TenantStatus.ACTIVE,
        cmd.ownerEmail(),
        tempPassword);
  }

  private void ensureTenantAdminRole(String tenantCode) {
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
                  r.describe("Tenant owner — manages their own store");
                  r.assignTo(tenantCode);
                  return r;
                });
    role.setPermissions(Set.of(profileRead));
    roleRepository.save(role);
  }
}
