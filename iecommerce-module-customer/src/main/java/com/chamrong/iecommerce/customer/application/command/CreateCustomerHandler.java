package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.customer.CustomerCreatedEvent;
import com.chamrong.iecommerce.customer.application.CustomerMapper;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCustomerHandler {

  private final CustomerRepository customerRepository;
  private final CustomerMapper mapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public CustomerResponse handle(CreateCustomerCommand command) {
    var customer = new Customer();
    customer.setFirstName(command.firstName());
    customer.setLastName(command.lastName());
    customer.setEmail(command.email());
    customer.setPhoneNumber(command.phoneNumber());
    customer.setAuthUserId(command.authUserId());

    if (command.tenantId() != null && !command.tenantId().isEmpty()) {
      customer.setTenantId(command.tenantId());
    }

    var savedCustomer = customerRepository.save(customer);
    eventPublisher.publishEvent(
        new CustomerCreatedEvent(
            savedCustomer.getTenantId(), savedCustomer.getId(), savedCustomer.getEmail()));
    return mapper.toResponse(savedCustomer);
  }
}
