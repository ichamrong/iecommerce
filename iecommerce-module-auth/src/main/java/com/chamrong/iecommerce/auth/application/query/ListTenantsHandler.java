package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.application.dto.TenantResponse;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Returns all tenants for admin listing. Owner email is not stored on the tenant entity and is
 * omitted from the response.
 */
@Component
@RequiredArgsConstructor
public class ListTenantsHandler {

  private final TenantRepositoryPort tenantRepository;

  @Transactional(readOnly = true)
  public List<TenantResponse> handle() {
    return tenantRepository.findAll().stream().map(this::toResponse).toList();
  }

  private TenantResponse toResponse(Tenant t) {
    return new TenantResponse(t.getCode(), t.getName(), t.getPlan(), t.getStatus(), null, null);
  }
}
