package com.chamrong.iecommerce.auth.application.command.tenant;

import com.chamrong.iecommerce.auth.application.command.UpdateTenantPreferencesCommand;
import com.chamrong.iecommerce.auth.application.dto.TenantPreferencesResponse;
import com.chamrong.iecommerce.auth.domain.event.TenantPreferencesUpdatedEvent;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Updates visual preferences (logo, colours, font) for an existing tenant. */
@Component
@RequiredArgsConstructor
public class UpdateTenantPreferencesHandler {

  private final TenantRepositoryPort tenantRepository;
  private final ApplicationEventPublisher eventPublisher;

  public TenantPreferencesResponse handle(UpdateTenantPreferencesCommand cmd) {
    var tenant =
        tenantRepository
            .findByCode(cmd.tenantId())
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + cmd.tenantId()));

    var prefs = tenant.getPreferences();

    if (cmd.logoUrl() != null) prefs.setLogoUrl(cmd.logoUrl());
    if (cmd.primaryColor() != null) prefs.setPrimaryColor(cmd.primaryColor());
    if (cmd.secondaryColor() != null) prefs.setSecondaryColor(cmd.secondaryColor());
    if (cmd.fontFamily() != null) prefs.setFontFamily(cmd.fontFamily());

    tenantRepository.save(tenant);
    eventPublisher.publishEvent(new TenantPreferencesUpdatedEvent(cmd.tenantId()));

    return TenantPreferencesResponse.from(prefs);
  }
}
