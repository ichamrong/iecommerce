package com.chamrong.iecommerce.customer.application.command;

import com.chamrong.iecommerce.customer.application.dto.AddressResponse;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCustomerHandler {

  private final CustomerRepository customerRepository;

  public CreateCustomerHandler(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @Transactional
  public CustomerResponse handle(CreateCustomerCommand command) {
    var customer = new Customer();
    customer.setFirstName(command.firstName());
    customer.setLastName(command.lastName());
    customer.setEmail(command.email());
    customer.setPhoneNumber(command.phoneNumber());
    customer.setAuthUserId(command.authUserId());

    // Set tenant ID if not null (though ideally Context will set it via BaseTenantEntity)
    if (command.tenantId() != null && !command.tenantId().isEmpty()) {
      customer.setTenantId(command.tenantId());
    }

    var savedCustomer = customerRepository.save(customer);
    return toResponse(savedCustomer);
  }

  public static CustomerResponse toResponse(Customer p) {
    var addressResponses =
        p.getAddresses().stream()
            .map(
                a ->
                    new AddressResponse(
                        a.getId(),
                        a.getStreet(),
                        a.getCity(),
                        a.getState(),
                        a.getPostalCode(),
                        a.getCountry(),
                        a.isDefaultShipping()))
            .collect(Collectors.toList());

    return new CustomerResponse(
        p.getId(),
        p.getFirstName(),
        p.getLastName(),
        p.getEmail(),
        p.getPhoneNumber(),
        p.getAuthUserId(),
        addressResponses);
  }
}
