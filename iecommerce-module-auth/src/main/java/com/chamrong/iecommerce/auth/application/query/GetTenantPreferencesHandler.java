package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.application.dto.TenantPreferencesResponse;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetTenantPreferencesHandler {

  private final TenantRepositoryPort tenantRepository;

  @Transactional(readOnly = true)
  public TenantPreferencesResponse handle(String tenantId) {
    var tenant =
        tenantRepository
            .findByCode(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

    return TenantPreferencesResponse.from(tenant.getPreferences());
  }
}
