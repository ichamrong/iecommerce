package com.chamrong.iecommerce.auth.application.saga;

import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.command.user.RegisterUserHandler;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantProvisioningStatus;
import com.chamrong.iecommerce.auth.domain.TenantRepository;
import com.chamrong.iecommerce.auth.domain.event.TenantRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates the tenant provisioning process (Saga). Ensures consistency between local DB and
 * external IDP.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantProvisionSaga {

  private final TenantRepository tenantRepository;
  private final RegisterUserHandler registerUserHandler;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void execute(Tenant tenant, String ownerEmail, String rawPassword, boolean isSignup) {
    String tenantCode = tenant.getCode();
    log.info("Starting provisioning saga for tenant: {}", tenantCode);

    try {
      // 1. Initial State already saved by caller in a single transaction if needed,
      // but here we manage the lifecycle.
      tenant.setProvisioningStatus(TenantProvisioningStatus.INITIAL);
      tenantRepository.save(tenant);

      // 2. Register Owner in IDP (Keycloak)
      var ownerUsername = isSignup ? ownerEmail : ownerEmail.split("@")[0];
      var regCmd =
          new RegisterCommand(
              ownerUsername, ownerEmail, rawPassword, tenantCode, Role.ROLE_TENANT_ADMIN);

      log.debug("Step 2: Registering owner {} in IDP", ownerUsername);
      registerUserHandler.handle(regCmd);

      tenant.setProvisioningStatus(TenantProvisioningStatus.IDP_CREATED);
      tenantRepository.save(tenant);

      // 3. Finalize
      tenant.setProvisioningStatus(TenantProvisioningStatus.COMPLETED);
      tenantRepository.save(tenant);

      if (isSignup) {
        eventPublisher.publishEvent(
            new TenantRegisteredEvent(
                tenantCode, tenant.getName(), tenant.getPlan(), tenant.getStatus()));
      }

      log.info("Provisioning saga completed successfully for tenant: {}", tenantCode);

    } catch (Exception e) {
      log.error(
          "Provisioning saga failed for tenant: {}. Triggering compensation...", tenantCode, e);
      tenant.setProvisioningStatus(TenantProvisioningStatus.FAILED);
      tenantRepository.save(tenant);
      throw e; // Rethrow to rollback transaction
    }
  }
}
