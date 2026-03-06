package com.chamrong.iecommerce.auth.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantPlan;
import com.chamrong.iecommerce.auth.domain.TenantPreferences;
import com.chamrong.iecommerce.auth.domain.TenantProvisioningStatus;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import com.chamrong.iecommerce.auth.infrastructure.persistence.jpa.entity.TenantEntity;
import com.chamrong.iecommerce.auth.infrastructure.persistence.jpa.entity.TenantPreferencesEmbeddable;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TenantPersistenceMapperTest {

  private final TenantPersistenceMapper mapper = new TenantPersistenceMapper();

  @Test
  void toDomainShouldMapAllFields() {
    Instant now = Instant.now();
    TenantEntity entity = new TenantEntity();
    entity.setId(42L);
    entity.setCode("code-1");
    entity.setName("Tenant One");
    entity.setPlan(TenantPlan.ENTERPRISE);
    entity.setStatus(TenantStatus.ACTIVE);
    entity.setTrialEndsAt(now.plusSeconds(3600));
    entity.setProvisioningStatus(TenantProvisioningStatus.COMPLETED);
    entity.setEnabled(true);

    Tenant domain = mapper.toDomain(entity);

    assertThat(domain.getId()).isEqualTo(42L);
    assertThat(domain.getCode()).isEqualTo("code-1");
    assertThat(domain.getName()).isEqualTo("Tenant One");
    assertThat(domain.getPlan()).isEqualTo(TenantPlan.ENTERPRISE);
    assertThat(domain.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    assertThat(domain.getTrialEndsAt()).isEqualTo(entity.getTrialEndsAt());
    assertThat(domain.getProvisioningStatus()).isEqualTo(TenantProvisioningStatus.COMPLETED);
    assertThat(domain.isEnabled()).isTrue();
    TenantPreferences prefs = domain.getPreferences();
    assertThat(prefs).isNotNull();
  }

  @Test
  void toDomainShouldReturnNullWhenEntityIsNull() {
    Tenant domain = mapper.toDomain(null);

    assertThat(domain).isNull();
  }

  @Test
  void toDomainShouldUseDefaultPreferencesWhenEmbeddableIsNull() {
    TenantEntity entity = new TenantEntity();
    entity.setCode("code-4");
    entity.setName("Tenant Four");
    entity.setPlan(TenantPlan.FREE);
    entity.setStatus(TenantStatus.TRIAL);
    entity.setProvisioningStatus(TenantProvisioningStatus.INITIAL);
    entity.setPreferences(null);

    Tenant domain = mapper.toDomain(entity);

    assertThat(domain.getPreferences()).isNotNull();
  }

  @Test
  void toEntityShouldMapAllFields() {
    Instant now = Instant.now();
    TenantPreferences prefs = new TenantPreferences();
    prefs.setLogoUrl("logo.png");
    prefs.setPrimaryColor("#333333");
    prefs.setSecondaryColor("#444444");
    prefs.setFontFamily("Roboto");

    Tenant domain =
        Tenant.provision(
            "code-2",
            "Tenant Two",
            TenantPlan.ENTERPRISE,
            TenantStatus.ACTIVE,
            TenantProvisioningStatus.COMPLETED);
    domain.setId(99L);
    domain.setTrialEndsAt(now.plusSeconds(7200));
    domain.setPreferences(prefs);

    TenantEntity entity = mapper.toEntity(domain);

    assertThat(entity.getId()).isEqualTo(99L);
    assertThat(entity.getCode()).isEqualTo("code-2");
    assertThat(entity.getName()).isEqualTo("Tenant Two");
    assertThat(entity.getPlan()).isEqualTo(TenantPlan.ENTERPRISE);
    assertThat(entity.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    assertThat(entity.getTrialEndsAt()).isEqualTo(domain.getTrialEndsAt());
    assertThat(entity.getProvisioningStatus()).isEqualTo(TenantProvisioningStatus.COMPLETED);
    assertThat(entity.isEnabled()).isTrue();

    TenantPreferencesEmbeddable prefsEmb = entity.getPreferences();
    assertThat(prefsEmb.getLogoUrl()).isEqualTo("logo.png");
    assertThat(prefsEmb.getPrimaryColor()).isEqualTo("#333333");
    assertThat(prefsEmb.getSecondaryColor()).isEqualTo("#444444");
    assertThat(prefsEmb.getFontFamily()).isEqualTo("Roboto");
  }

  @Test
  void toEntityShouldReturnNullWhenTenantIsNull() {
    TenantEntity entity = mapper.toEntity(null);

    assertThat(entity).isNull();
  }
}
