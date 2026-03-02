package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.customer.AddressUpdatedEvent;
import com.chamrong.iecommerce.customer.application.CustomerMapper;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.dto.UpdateAddressRequest;
import com.chamrong.iecommerce.customer.domain.ports.CustomerRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateAddressHandler {

  private final CustomerRepositoryPort customerRepository;
  private final CustomerMapper mapper;
  private final ApplicationEventPublisher eventPublisher;

  public CustomerResponse handle(Long customerId, Long addressId, UpdateAddressRequest request) {
    var customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));

    var address =
        customer.getAddresses().stream()
            .filter(a -> a.getId().equals(addressId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Address not found: " + addressId));

    if (request.street() != null) address.setStreet(request.street());
    if (request.city() != null) address.setCity(request.city());
    if (request.state() != null) address.setState(request.state());
    if (request.postalCode() != null) address.setPostalCode(request.postalCode());
    if (request.country() != null) address.setCountry(request.country());

    if (request.isDefaultShipping() != null && request.isDefaultShipping()) {
      customer.getAddresses().forEach(a -> a.setDefaultShipping(false));
      address.setDefaultShipping(true);
    } else if (request.isDefaultShipping() != null) {
      address.setDefaultShipping(false);
    }

    if (request.isDefaultBilling() != null && request.isDefaultBilling()) {
      customer.getAddresses().forEach(a -> a.setDefaultBilling(false));
      address.setDefaultBilling(true);
    } else if (request.isDefaultBilling() != null) {
      address.setDefaultBilling(false);
    }

    var saved = customerRepository.save(customer);
    eventPublisher.publishEvent(
        new AddressUpdatedEvent(saved.getTenantId(), customerId, addressId));
    return mapper.toResponse(saved);
  }
}
