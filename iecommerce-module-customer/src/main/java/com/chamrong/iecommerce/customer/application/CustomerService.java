package com.chamrong.iecommerce.customer.application;

import com.chamrong.iecommerce.common.pagination.CursorCodec;
import com.chamrong.iecommerce.common.pagination.CursorPageResponse;
import com.chamrong.iecommerce.common.pagination.CursorPayload;
import com.chamrong.iecommerce.common.pagination.FilterHasher;
import com.chamrong.iecommerce.common.pagination.InvalidCursorException;
import com.chamrong.iecommerce.common.security.TenantGuard;
import com.chamrong.iecommerce.customer.CustomerApi;
import com.chamrong.iecommerce.customer.application.dto.AddAddressRequest;
import com.chamrong.iecommerce.customer.application.dto.AddressResponse;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.dto.UpdateCustomerRequest;
import com.chamrong.iecommerce.customer.domain.Address;
import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.ports.CustomerRepositoryPort;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService implements CustomerApi {

  public static final String ENDPOINT_LIST_CUSTOMERS = "customer:listCustomers";

  private final CustomerRepositoryPort customerRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<com.chamrong.iecommerce.customer.CustomerInfo> getCustomer(
      String tenantId, Long id) {
    return customerRepository
        .findById(id)
        .map(
            c -> {
              TenantGuard.requireSameTenant(c.getTenantId(), tenantId);
              return new com.chamrong.iecommerce.customer.CustomerInfo(
                  c.getId(), c.getFirstName(), c.getEmail());
            });
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<CustomerResponse> listCustomers(
      String tenantId, String cursorStr, int limit) {
    int effectiveLimit = Math.min(Math.max(limit, 1), 100);
    int fetchSize = effectiveLimit + 1;
    Map<String, Object> filterMap = new LinkedHashMap<>();
    filterMap.put("_endpoint", ENDPOINT_LIST_CUSTOMERS);
    String filterHash = FilterHasher.computeHash(ENDPOINT_LIST_CUSTOMERS, filterMap);

    java.time.Instant afterCreatedAt = null;
    Long afterId = null;
    if (cursorStr != null && !cursorStr.isBlank()) {
      CursorPayload payload = CursorCodec.decodeAndValidateFilter(cursorStr, filterHash);
      afterCreatedAt = payload.getCreatedAt();
      try {
        afterId = Long.valueOf(payload.getId());
      } catch (NumberFormatException e) {
        throw new InvalidCursorException(
            InvalidCursorException.INVALID_CURSOR, "Invalid cursor id");
      }
    }

    List<Customer> customers =
        customerRepository.findCursorPage(tenantId, afterCreatedAt, afterId, fetchSize);

    boolean hasNext = customers.size() == fetchSize;
    List<Customer> dataPage = hasNext ? customers.subList(0, effectiveLimit) : customers;

    String nextCursor = null;
    if (hasNext && !dataPage.isEmpty()) {
      Customer lastItem = dataPage.get(dataPage.size() - 1);
      nextCursor =
          CursorCodec.encode(
              new CursorPayload(
                  1, lastItem.getCreatedAt(), String.valueOf(lastItem.getId()), filterHash));
    }

    List<CustomerResponse> dtoList = dataPage.stream().map(this::mapToResponse).toList();
    return CursorPageResponse.of(dtoList, nextCursor, hasNext, effectiveLimit);
  }

  @Override
  @Transactional(readOnly = true)
  public CustomerResponse getCustomerFull(String tenantId, Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    TenantGuard.requireSameTenant(customer.getTenantId(), tenantId);
    return mapToResponse(customer);
  }

  @Override
  @Transactional
  public CustomerResponse updateCustomer(String tenantId, Long id, UpdateCustomerRequest req) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    TenantGuard.requireSameTenant(customer.getTenantId(), tenantId);

    customer.setFirstName(req.firstName());
    customer.setLastName(req.lastName());
    customer.setPhoneNumber(req.phoneNumber());
    customer.setDateOfBirth(req.dateOfBirth());
    customer.setGender(req.gender());

    return mapToResponse(customerRepository.save(customer));
  }

  @Override
  @Transactional
  public void blockCustomer(String tenantId, Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    TenantGuard.requireSameTenant(customer.getTenantId(), tenantId);
    customer.block();
    customerRepository.save(customer);
  }

  @Override
  @Transactional
  public void unblockCustomer(String tenantId, Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    TenantGuard.requireSameTenant(customer.getTenantId(), tenantId);
    customer.unblock();
    customerRepository.save(customer);
  }

  @Override
  @Transactional
  public void addAddress(String tenantId, Long customerId, AddAddressRequest req) {
    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    TenantGuard.requireSameTenant(customer.getTenantId(), tenantId);

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
  public void removeAddress(String tenantId, Long customerId, Long addressId) {
    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    TenantGuard.requireSameTenant(customer.getTenantId(), tenantId);

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
