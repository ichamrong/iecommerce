package com.chamrong.iecommerce.auth.application.command.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.command.TenantSignupCommand;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.application.saga.TenantProvisionSaga;
import com.chamrong.iecommerce.auth.domain.Permission;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantPlan;
import com.chamrong.iecommerce.auth.domain.TenantProvisioningStatus;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import com.chamrong.iecommerce.auth.domain.ports.PermissionRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantSignupHandlerTest {

  @Mock private TenantRepositoryPort tenantRepository;
  @Mock private RoleRepositoryPort roleRepository;
  @Mock private PermissionRepositoryPort permissionRepository;
  @Mock private TenantProvisionSaga provisionSaga;

  @InjectMocks private TenantSignupHandler handler;

  @Test
  void handleShouldThrowWhenTenantCodeAlreadyExists() {
    var cmd = new TenantSignupCommand("My Shop", "owner", "owner@example.com", "password");
    String slug = TenantSignupHandler.slugify(cmd.shopName());

    when(tenantRepository.existsByCode(slug)).thenReturn(true);

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(DuplicateUserException.class)
        .hasMessageContaining("Shop name already taken");

    verifyNoInteractions(provisionSaga);
  }

  @Test
  void handleShouldCreateTenantEnsureAdminRoleAndStartProvisioningSaga() {
    var cmd = new TenantSignupCommand("My Shop", "owner", "owner@example.com", "password");
    String slug = TenantSignupHandler.slugify(cmd.shopName());

    when(tenantRepository.existsByCode(slug)).thenReturn(false);

    // Saved tenant echo
    Tenant savedTenant = Tenant.signup(slug, cmd.shopName(), java.time.Instant.now());
    savedTenant.setProvisioningStatus(TenantProvisioningStatus.INITIAL);
    when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

    // ROLE_TENANT_ADMIN does not exist initially and PROFILE_READ permission will be created
    when(roleRepository.findByName(Role.ROLE_TENANT_ADMIN)).thenReturn(Optional.empty());
    when(permissionRepository.findByName(
            com.chamrong.iecommerce.auth.domain.Permissions.PROFILE_READ))
        .thenReturn(Optional.empty());
    when(permissionRepository.save(any(Permission.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = handler.handle(cmd);

    // Response reflects FREE TRIAL tenant
    assertThat(response.tenantCode()).isEqualTo(slug);
    assertThat(response.shopName()).isEqualTo(cmd.shopName());
    assertThat(response.plan()).isEqualTo(TenantPlan.FREE);
    assertThat(response.status()).isEqualTo(TenantStatus.TRIAL);
    assertThat(response.ownerEmail()).isEqualTo(cmd.ownerEmail());

    // Tenant saved and saga executed
    verify(tenantRepository).save(any(Tenant.class));
    verify(provisionSaga).execute(savedTenant, cmd.ownerEmail(), cmd.ownerPassword(), true);

    // ROLE_TENANT_ADMIN gets persisted with permissions
    ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
    verify(roleRepository).save(roleCaptor.capture());
    Role role = roleCaptor.getValue();
    assertThat(role.getName()).isEqualTo(Role.ROLE_TENANT_ADMIN);
    assertThat(role.getPermissions()).hasSize(1);
  }
}
