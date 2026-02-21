package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.application.dto.TenantPreferencesResponse;
import com.chamrong.iecommerce.auth.domain.TenantPreferences;
import com.chamrong.iecommerce.auth.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateTenantPreferencesHandler {

  private final TenantRepository tenantRepository;

  @Transactional
  public TenantPreferencesResponse handle(UpdateTenantPreferencesCommand cmd) {
    var tenant =
        tenantRepository
            .findByCode(cmd.tenantId())
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + cmd.tenantId()));

    TenantPreferences prefs = tenant.getPreferences();
    if (prefs == null) {
      prefs = new TenantPreferences();
      tenant.setPreferences(prefs);
    }

    if (cmd.logoUrl() != null) prefs.setLogoUrl(cmd.logoUrl());
    if (cmd.primaryColor() != null) prefs.setPrimaryColor(cmd.primaryColor());
    if (cmd.secondaryColor() != null) prefs.setSecondaryColor(cmd.secondaryColor());
    if (cmd.fontFamily() != null) prefs.setFontFamily(cmd.fontFamily());

    tenantRepository.save(tenant);

    return TenantPreferencesResponse.from(prefs);
  }
}
