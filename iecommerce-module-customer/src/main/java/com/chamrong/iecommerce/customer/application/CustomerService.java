package com.chamrong.iecommerce.customer.application;

import com.chamrong.iecommerce.customer.CustomerApi;
import com.chamrong.iecommerce.customer.api.dto.CursorResponse;
import com.chamrong.iecommerce.customer.api.util.CursorEncoder;
import com.chamrong.iecommerce.customer.application.dto.AddAddressRequest;
import com.chamrong.iecommerce.customer.application.dto.AddressResponse;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.dto.UpdateCustomerRequest;
import com.chamrong.iecommerce.customer.domain.Address;
import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService implements CustomerApi {

  private final CustomerRepository customerRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<com.chamrong.iecommerce.customer.CustomerInfo> getCustomer(Long id) {
    return customerRepository
        .findById(id)
        .map(
            c ->
                new com.chamrong.iecommerce.customer.CustomerInfo(
                    c.getId(), c.getFirstName(), c.getEmail()));
  }

  @Override
  @Transactional(readOnly = true)
  public CursorResponse<CustomerResponse> listCustomers(String cursorStr, int limit) {
    CursorEncoder.Cursor cursor = CursorEncoder.decode(cursorStr);

    // Fetch limit + 1 to know if there is a next page
    List<Customer> customers = customerRepository.findNextPage(cursor, limit + 1);

    boolean hasNext = customers.size() > limit;
    List<Customer> dataPage = hasNext ? customers.subList(0, limit) : customers;

    String nextCursor = null;
    if (hasNext && !dataPage.isEmpty()) {
      Customer lastItem = dataPage.get(dataPage.size() - 1);
      nextCursor = CursorEncoder.encode(lastItem.getCreatedAt(), lastItem.getId());
    }

    List<CustomerResponse> dtoList = dataPage.stream().map(this::mapToResponse).toList();
    return new CursorResponse<>(dtoList, nextCursor, hasNext);
  }

  @Override
  @Transactional(readOnly = true)
  public CustomerResponse getCustomerFull(Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    return mapToResponse(customer);
  }

  @Override
  @Transactional
  public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest req) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    customer.setFirstName(req.firstName());
    customer.setLastName(req.lastName());
    customer.setPhoneNumber(req.phoneNumber());
    customer.setDateOfBirth(req.dateOfBirth());
    customer.setGender(req.gender());

    return mapToResponse(customerRepository.save(customer));
  }

  @Override
  @Transactional
  public void blockCustomer(Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    customer.block();
    customerRepository.save(customer);
  }

  @Override
  @Transactional
  public void unblockCustomer(Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    customer.unblock();
    customerRepository.save(customer);
  }

  @Override
  @Transactional
  public void addAddress(Long customerId, AddAddressRequest req) {
    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    Address address = new Address();
    address.setStreet(req.street());
    address.setCity(req.city());
    address.setState(req.state());
    address.setPostalCode(req.postalCode());
    address.setCountry(req.country());
    address.setDefaultShipping(req.isDefaultShipping());
    address.setDefaultBilling(req.isDefaultBilling());

    customer.addAddress(address);
    customerRepository.save(customer);
  }

  @Override
  @Transactional
  public void removeAddress(Long customerId, Long addressId) {
    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    customer.getAddresses().removeIf(a -> a.getId().equals(addressId));
    customerRepository.save(customer);
  }

  private CustomerResponse mapToResponse(Customer c) {
    return new CustomerResponse(
        c.getId(),
        c.getFirstName(),
        c.getLastName(),
        c.getEmail(),
        c.getPhoneNumber(),
        c.getAuthUserId(),
        c.getStatus().name(),
        c.getLoyaltyTier().name(),
        c.getLoyaltyPoints(),
        c.getDateOfBirth(),
        c.getGender(),
        c.getAddresses().stream()
            .map(
                a ->
                    new AddressResponse(
                        a.getId(),
                        a.getStreet(),
                        a.getCity(),
                        a.getState(),
                        a.getPostalCode(),
                        a.getCountry(),
                        a.isDefaultShipping(),
                        a.isDefaultBilling()))
            .collect(Collectors.toList()));
  }
}
