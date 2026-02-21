package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.customer.AddressRemovedEvent;
import com.chamrong.iecommerce.customer.application.CustomerMapper;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RemoveAddressHandler {

  private final CustomerRepository customerRepository;
  private final CustomerMapper mapper;
  private final ApplicationEventPublisher eventPublisher;

  public CustomerResponse handle(Long customerId, Long addressId) {
    var customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));

    customer.getAddresses().removeIf(a -> a.getId().equals(addressId));

    var saved = customerRepository.save(customer);
    eventPublisher.publishEvent(
        new AddressRemovedEvent(saved.getTenantId(), customerId, addressId));
    return mapper.toResponse(saved);
  }
}
