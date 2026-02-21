package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.customer.CustomerUpdatedEvent;
import com.chamrong.iecommerce.customer.application.CustomerMapper;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.dto.UpdateCustomerRequest;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateCustomerHandler {

  private final CustomerRepository customerRepository;
  private final CustomerMapper mapper;
  private final ApplicationEventPublisher eventPublisher;

  public CustomerResponse handle(Long id, UpdateCustomerRequest request) {
    var customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));

    if (request.firstName() != null) customer.setFirstName(request.firstName());
    if (request.lastName() != null) customer.setLastName(request.lastName());
    if (request.phoneNumber() != null) customer.setPhoneNumber(request.phoneNumber());
    if (request.dateOfBirth() != null) customer.setDateOfBirth(request.dateOfBirth());
    if (request.gender() != null) customer.setGender(request.gender());

    var saved = customerRepository.save(customer);
    eventPublisher.publishEvent(new CustomerUpdatedEvent(saved.getTenantId(), saved.getId()));
    return mapper.toResponse(saved);
  }
}
