package com.chamrong.iecommerce.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TenantTest {

  @Test
  void signupShouldInitializeTrialTenantWithDefaults() {
    Instant trialEndsAt = Instant.now().plusSeconds(3600);

    Tenant tenant = Tenant.signup("code-1", "Tenant One", trialEndsAt);

    assertThat(tenant.getCode()).isEqualTo("code-1");
    assertThat(tenant.getName()).isEqualTo("Tenant One");
    assertThat(tenant.getPlan()).isEqualTo(TenantPlan.FREE);
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.TRIAL);
    assertThat(tenant.getTrialEndsAt()).isEqualTo(trialEndsAt);
    assertThat(tenant.getProvisioningStatus()).isEqualTo(TenantProvisioningStatus.INITIAL);
    assertThat(tenant.isEnabled()).isTrue();
  }

  @Test
  void provisionShouldRespectStatusAndPlanAndEnabledFlag() {
    Tenant tenant =
        Tenant.provision(
            "code-2",
            "Tenant Two",
            TenantPlan.ENTERPRISE,
            TenantStatus.ACTIVE,
            TenantProvisioningStatus.COMPLETED);

    assertThat(tenant.getPlan()).isEqualTo(TenantPlan.ENTERPRISE);
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    assertThat(tenant.getProvisioningStatus()).isEqualTo(TenantProvisioningStatus.COMPLETED);
    assertThat(tenant.isEnabled()).isTrue();

    tenant =
        Tenant.provision(
            "code-3",
            "Tenant Three",
            TenantPlan.FREE,
            TenantStatus.SUSPENDED,
            TenantProvisioningStatus.INITIAL);

    assertThat(tenant.isEnabled()).isFalse();
  }

  @Test
  void activateAndSuspendShouldUpdateStatusAndEnabled() {
    Tenant tenant = new Tenant("code-4", "Tenant Four");

    tenant.activate();
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    assertThat(tenant.isEnabled()).isTrue();

    tenant.suspend();
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
    assertThat(tenant.isEnabled()).isFalse();
  }

  @Test
  void updateStatusShouldDriveEnabledFlag() {
    Tenant tenant = new Tenant("code-5", "Tenant Five");

    tenant.updateStatus(TenantStatus.TRIAL);
    assertThat(tenant.isEnabled()).isTrue();

    tenant.updateStatus(TenantStatus.GRACE);
    assertThat(tenant.isEnabled()).isTrue();

    tenant.updateStatus(TenantStatus.SUSPENDED);
    assertThat(tenant.isEnabled()).isFalse();
  }
}
