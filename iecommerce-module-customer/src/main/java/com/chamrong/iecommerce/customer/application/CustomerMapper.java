package com.chamrong.iecommerce.customer.application;

import com.chamrong.iecommerce.customer.application.dto.AddressResponse;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.domain.Address;
import com.chamrong.iecommerce.customer.domain.Customer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

  public CustomerResponse toResponse(Customer p) {
    if (p == null) return null;

    var addressResponses =
        p.getAddresses().stream().map(this::toAddressResponse).collect(Collectors.toList());

    return new CustomerResponse(
        p.getId(),
        p.getFirstName(),
        p.getLastName(),
        p.getEmail(),
        p.getPhoneNumber(),
        p.getAuthUserId(),
        p.getStatus().name(),
        p.getLoyaltyTier().name(),
        p.getLoyaltyPoints(),
        p.getDateOfBirth(),
        p.getGender(),
        addressResponses);
  }

  public AddressResponse toAddressResponse(Address a) {
    if (a == null) return null;
    return new AddressResponse(
        a.getId(),
        a.getStreet(),
        a.getCity(),
        a.getState(),
        a.getPostalCode(),
        a.getCountry(),
        a.isDefaultShipping(),
        a.isDefaultBilling());
  }
}
