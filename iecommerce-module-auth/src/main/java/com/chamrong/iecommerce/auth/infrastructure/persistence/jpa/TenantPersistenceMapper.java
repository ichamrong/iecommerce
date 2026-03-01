package com.chamrong.iecommerce.auth.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantPreferences;
import com.chamrong.iecommerce.auth.infrastructure.persistence.jpa.entity.TenantEntity;
import com.chamrong.iecommerce.auth.infrastructure.persistence.jpa.entity.TenantPreferencesEmbeddable;
import org.springframework.stereotype.Component;

/** Maps between Tenant (domain) and TenantEntity (persistence). */
@Component
public class TenantPersistenceMapper {

  public Tenant toDomain(TenantEntity entity) {
    if (entity == null) {
      return null;
    }
    Tenant t = new Tenant(entity.getCode(), entity.getName());
    t.setId(entity.getId());
    t.setCreatedAt(entity.getCreatedAt());
    t.setUpdatedAt(entity.getUpdatedAt());
    t.setDeleted(entity.isDeleted());
    t.setDeletedAt(entity.getDeletedAt());
    t.setPlan(entity.getPlan());
    t.setStatus(entity.getStatus());
    t.setTrialEndsAt(entity.getTrialEndsAt());
    t.setPreferences(toDomainPreferences(entity.getPreferences()));
    t.setProvisioningStatus(entity.getProvisioningStatus());
    t.setEnabled(entity.isEnabled());
    return t;
  }

  public TenantEntity toEntity(Tenant tenant) {
    if (tenant == null) {
      return null;
    }
    TenantEntity e = new TenantEntity();
    if (tenant.getId() != null) {
      e.setId(tenant.getId());
    }
    e.setCode(tenant.getCode());
    e.setName(tenant.getName());
    e.setPlan(tenant.getPlan());
    e.setStatus(tenant.getStatus());
    e.setTrialEndsAt(tenant.getTrialEndsAt());
    e.setPreferences(toEntityPreferences(tenant.getPreferences()));
    e.setProvisioningStatus(tenant.getProvisioningStatus());
    e.setEnabled(tenant.isEnabled());
    return e;
  }

  private static TenantPreferences toDomainPreferences(TenantPreferencesEmbeddable emb) {
    if (emb == null) {
      return new TenantPreferences();
    }
    TenantPreferences p = new TenantPreferences();
    p.setLogoUrl(emb.getLogoUrl());
    p.setPrimaryColor(emb.getPrimaryColor());
    p.setSecondaryColor(emb.getSecondaryColor());
    p.setFontFamily(emb.getFontFamily());
    return p;
  }

  private static TenantPreferencesEmbeddable toEntityPreferences(TenantPreferences prefs) {
    if (prefs == null) {
      return new TenantPreferencesEmbeddable();
    }
    TenantPreferencesEmbeddable emb = new TenantPreferencesEmbeddable();
    emb.setLogoUrl(prefs.getLogoUrl());
    emb.setPrimaryColor(prefs.getPrimaryColor());
    emb.setSecondaryColor(prefs.getSecondaryColor());
    emb.setFontFamily(prefs.getFontFamily());
    return emb;
  }
}
