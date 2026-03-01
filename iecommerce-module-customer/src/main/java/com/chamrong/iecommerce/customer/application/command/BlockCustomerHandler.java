package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.common.security.TenantGuard;
import com.chamrong.iecommerce.customer.CustomerBlockedEvent;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BlockCustomerHandler {

  private final CustomerRepository customerRepository;
  private final ApplicationEventPublisher eventPublisher;

  public void handle(String tenantId, Long id) {
    var customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    TenantGuard.requireSameTenant(customer.getTenantId(), tenantId);
    customer.block();
    customerRepository.save(customer);
    eventPublisher.publishEvent(new CustomerBlockedEvent(customer.getTenantId(), id));
  }
}
