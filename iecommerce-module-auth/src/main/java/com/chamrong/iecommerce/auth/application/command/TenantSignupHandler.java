package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.application.dto.TenantResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.PermissionRepository;
import com.chamrong.iecommerce.auth.domain.Permissions;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.RoleRepository;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantPlan;
import com.chamrong.iecommerce.auth.domain.TenantRepository;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import com.chamrong.iecommerce.common.TenantContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles self-service tenant signup.
 *
 * <p>Creates a FREE tenant on a 30-day TRIAL and its owner account atomically via Keycloak.
 */
@Component
public class TenantSignupHandler {

  private final TenantRepository tenantRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final RegisterUserHandler registerUserHandler;

  public TenantSignupHandler(
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
  public TenantResponse handle(TenantSignupCommand cmd) {
    String tenantCode = slugify(cmd.shopName());

    if (tenantRepository.existsByCode(tenantCode)) {
      throw new DuplicateUserException("Shop name already taken: " + cmd.shopName());
    }

    // 1. Create the tenant on a 30-day Trial
    Tenant tenant = new Tenant();
    tenant.setCode(tenantCode);
    tenant.setName(cmd.shopName());
    tenant.setPlan(TenantPlan.FREE);
    tenant.setStatus(TenantStatus.TRIAL);
    tenant.setTrialEndsAt(Instant.now().plus(30, ChronoUnit.DAYS));
    tenantRepository.save(tenant);

    // 2. Create ROLE_TENANT_ADMIN locally scoped to this Tenant
    TenantContext.setCurrentTenant(tenantCode);
    try {
      ensureTenantAdminRole(tenantCode);

      // 3. Register the owner user inside Keycloak and sync to local DB
      RegisterCommand regCmd =
          new RegisterCommand(
              cmd.ownerUsername(),
              cmd.ownerEmail(),
              cmd.ownerPassword(),
              tenantCode,
              Role.ROLE_TENANT_ADMIN);
      registerUserHandler.handle(regCmd);
    } finally {
      TenantContext.clear();
    }

    return new TenantResponse(
        tenantCode, cmd.shopName(), TenantPlan.FREE, TenantStatus.TRIAL, cmd.ownerEmail(), null);
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

  /** Converts "My Shop Name" → "my_shop_name" */
  static String slugify(String name) {
    return name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
  }
}
