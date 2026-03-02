package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.common.security.TenantGuard;
import com.chamrong.iecommerce.customer.CustomerUnblockedEvent;
import com.chamrong.iecommerce.customer.domain.ports.CustomerRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UnblockCustomerHandler {

  private final CustomerRepositoryPort customerRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(String tenantId, Long id) {
    var customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    TenantGuard.requireSameTenant(customer.getTenantId(), tenantId);
    customer.unblock();
    customerRepository.save(customer);
    eventPublisher.publishEvent(new CustomerUnblockedEvent(customer.getTenantId(), id));
  }
}
