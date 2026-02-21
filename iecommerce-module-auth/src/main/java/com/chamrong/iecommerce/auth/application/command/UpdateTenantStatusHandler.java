package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.TenantStatusUpdatedEvent;
import com.chamrong.iecommerce.auth.domain.TenantRepository;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateTenantStatusHandler {

  private final TenantRepository tenantRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void handle(UpdateTenantStatusCommand cmd) {
    var tenant =
        tenantRepository
            .findByCode(cmd.tenantId())
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + cmd.tenantId()));

    tenant.setStatus(cmd.status());

    if (cmd.status() == TenantStatus.DISABLED) {
      tenant.setEnabled(false);
    } else if (cmd.status() == TenantStatus.ACTIVE || cmd.status() == TenantStatus.TRIAL) {
      tenant.setEnabled(true);
    }

    tenantRepository.save(tenant);
    eventPublisher.publishEvent(new TenantStatusUpdatedEvent(cmd.tenantId(), cmd.status()));

    // In the future: emit TenantSuspendedEvent to freeze storefront resources dynamically.
  }
}
