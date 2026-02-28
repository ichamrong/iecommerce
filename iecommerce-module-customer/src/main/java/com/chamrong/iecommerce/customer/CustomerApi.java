package com.chamrong.iecommerce.customer;

import com.chamrong.iecommerce.customer.application.dto.AddAddressRequest;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.dto.UpdateCustomerRequest;
import java.util.Optional;

/** Public API of the Customer module. */
public interface CustomerApi {

  /** Returns a customer by ID. */
  Optional<CustomerInfo> getCustomer(Long id);

  com.chamrong.iecommerce.customer.api.dto.CursorResponse<CustomerResponse> listCustomers(
      String cursor, int limit);

  CustomerResponse getCustomerFull(Long id);

  CustomerResponse updateCustomer(Long id, UpdateCustomerRequest req);

  void blockCustomer(Long id);

  void unblockCustomer(Long id);

  void addAddress(Long customerId, AddAddressRequest req);

  void removeAddress(Long customerId, Long addressId);
}
