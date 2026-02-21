package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.customer.AddressAddedEvent;
import com.chamrong.iecommerce.customer.application.CustomerMapper;
import com.chamrong.iecommerce.customer.application.dto.AddAddressRequest;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.domain.Address;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AddAddressHandler {

  private final CustomerRepository customerRepository;
  private final CustomerMapper mapper;
  private final ApplicationEventPublisher eventPublisher;

  public CustomerResponse handle(Long customerId, AddAddressRequest request) {
    var customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));

    var address = new Address();
    address.setStreet(request.street());
    address.setCity(request.city());
    address.setState(request.state());
    address.setPostalCode(request.postalCode());
    address.setCountry(request.country());
    address.setDefaultShipping(request.isDefaultShipping());
    address.setDefaultBilling(request.isDefaultBilling());

    customer.addAddress(address);
    var saved = customerRepository.save(customer);
    eventPublisher.publishEvent(
        new AddressAddedEvent(
            saved.getTenantId(), customerId, address.getStreet() + ", " + address.getCity()));
    return mapper.toResponse(saved);
  }
}
