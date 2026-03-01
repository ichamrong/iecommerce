package com.chamrong.iecommerce.auth.application.command.tenant;

import com.chamrong.iecommerce.auth.application.command.UpdateTenantStatusCommand;
import com.chamrong.iecommerce.auth.domain.event.TenantStatusUpdatedEvent;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Updates the lifecycle status of a tenant (ACTIVE, DISABLED, TRIAL, SUSPENDED). */
@Component
@RequiredArgsConstructor
public class UpdateTenantStatusHandler {

  private final TenantRepositoryPort tenantRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void handle(UpdateTenantStatusCommand cmd) {
    var tenant =
        tenantRepository
            .findByCode(cmd.tenantId())
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + cmd.tenantId()));

    tenant.updateStatus(cmd.status());

    tenantRepository.save(tenant);
    eventPublisher.publishEvent(new TenantStatusUpdatedEvent(cmd.tenantId(), cmd.status()));

    // In the future: emit TenantSuspendedEvent to freeze storefront resources dynamically.
  }
}
