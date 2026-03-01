package com.chamrong.iecommerce.auth.application.command.tenant;

import com.chamrong.iecommerce.auth.application.command.TenantSignupCommand;
import com.chamrong.iecommerce.auth.application.dto.TenantResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.application.saga.TenantProvisionSaga;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantPlan;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import com.chamrong.iecommerce.auth.domain.ports.PermissionRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import com.chamrong.iecommerce.common.annotation.WithTenantContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles self-service tenant signup.
 *
 * <p>Creates a FREE tenant on a 30-day TRIAL and its owner account atomically via Keycloak.
 */
@Component
@RequiredArgsConstructor
public class TenantSignupHandler {

  private final TenantRepositoryPort tenantRepository;
  private final RoleRepositoryPort roleRepository;
  private final PermissionRepositoryPort permissionRepository;
  private final TenantProvisionSaga provisionSaga;

  @Transactional
  @WithTenantContext(
      tenantId =
          "T(com.chamrong.iecommerce.auth.application.command.tenant.TenantSignupHandler).slugify(#cmd.shopName())")
  public TenantResponse handle(TenantSignupCommand cmd) {
    var tenantCode = slugify(cmd.shopName());

    if (tenantRepository.existsByCode(tenantCode)) {
      throw new DuplicateUserException("Shop name already taken: " + cmd.shopName());
    }

    // 1. Create the tenant shell
    var tenant = Tenant.signup(tenantCode, cmd.shopName(), Instant.now().plus(30, ChronoUnit.DAYS));
    var savedTenant = tenantRepository.save(tenant);

    // 2. Ensure local ROLE_TENANT_ADMIN exists
    ensureTenantAdminRole(tenantCode);

    // 3. Delegate distributed provisioning to Saga
    provisionSaga.execute(savedTenant, cmd.ownerEmail(), cmd.ownerPassword(), true);

    return new TenantResponse(
        tenantCode, cmd.shopName(), TenantPlan.FREE, TenantStatus.TRIAL, cmd.ownerEmail(), null);
  }

  private void ensureTenantAdminRole(String tenantCode) {
    var profileRead =
        permissionRepository
            .findByName(Permissions.PROFILE_READ)
            .orElseGet(() -> permissionRepository.save(new Permission(Permissions.PROFILE_READ)));

    var role =
        roleRepository
            .findByName(Role.ROLE_TENANT_ADMIN)
            .orElseGet(
                () -> {
                  var r = new Role(Role.ROLE_TENANT_ADMIN);
                  r.describe("Tenant owner — manages their own store");
                  r.assignTo(tenantCode);
                  return r;
                });
    role.setPermissions(Set.of(profileRead));
    roleRepository.save(role);
  }

  /** Converts "My Shop Name" → "my_shop_name" */
  public static String slugify(String name) {
    return name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
  }
}
