package com.chamrong.iecommerce.auth.application.command.tenant;

import com.chamrong.iecommerce.auth.application.command.UpdateTenantCommand;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Updates tenant name and/or plan. Used by admin PUT /api/v1/admin/tenants/:id. */
@Component
@RequiredArgsConstructor
public class UpdateTenantHandler {

  private final TenantRepositoryPort tenantRepository;

  @Transactional
  public void handle(UpdateTenantCommand cmd) {
    Tenant tenant =
        tenantRepository
            .findByCode(cmd.tenantCode())
            .orElseThrow(
                () -> new IllegalArgumentException("Tenant not found: " + cmd.tenantCode()));
    if (cmd.name() != null && !cmd.name().isBlank()) {
      tenant.setName(cmd.name());
    }
    if (cmd.plan() != null) {
      tenant.updatePlan(cmd.plan(), cmd.trialEndsAt());
    }
    tenantRepository.save(tenant);
  }
}
