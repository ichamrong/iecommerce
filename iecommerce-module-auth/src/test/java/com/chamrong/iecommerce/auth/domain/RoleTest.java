package com.chamrong.iecommerce.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Role} basic behavior. */
class RoleTest {

  private static final String TENANT_ID = "TENANT-1";

  @Test
  void describeShouldUpdateDescription() {
    Role role = new Role(Role.ROLE_TENANT_ADMIN);

    role.describe("Tenant admin role");

    assertThat(role.getDescription()).isEqualTo("Tenant admin role");
  }

  @Test
  void assignToShouldSetTenantId() {
    Role role = new Role(Role.ROLE_TENANT_ADMIN);

    role.assignTo(TENANT_ID);

    assertThat(role.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void setPermissionsShouldReplacePermissionsSet() {
    Role role = new Role(Role.ROLE_TENANT_ADMIN);
    Permission permission = new Permission(Permissions.PROFILE_READ);

    role.setPermissions(Set.of(permission));

    assertThat(role.getPermissions()).containsExactly(permission);
  }
}
