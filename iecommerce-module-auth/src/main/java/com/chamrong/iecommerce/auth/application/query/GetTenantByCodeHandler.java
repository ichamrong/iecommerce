package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.application.dto.TenantResponse;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns a single tenant by code for admin detail view. Owner email is not stored on the tenant
 * entity and is omitted.
 */
@Component
@RequiredArgsConstructor
public class GetTenantByCodeHandler {

  private final TenantRepositoryPort tenantRepository;

  @Transactional(readOnly = true)
  public Optional<TenantResponse> handle(String tenantCode) {
    return tenantRepository.findByCode(tenantCode).map(this::toResponse);
  }

  private TenantResponse toResponse(Tenant t) {
    return new TenantResponse(t.getCode(), t.getName(), t.getPlan(), t.getStatus(), null, null);
  }
}
