package com.chamrong.iecommerce.customer;

import com.chamrong.iecommerce.customer.application.dto.AddAddressRequest;
import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.application.dto.UpdateCustomerRequest;
import java.util.Optional;

/** Public API of the Customer module. All operations are tenant-scoped. */
public interface CustomerApi {

  Optional<CustomerInfo> getCustomer(String tenantId, Long id);

  com.chamrong.iecommerce.customer.api.dto.CursorResponse<CustomerResponse> listCustomers(
      String tenantId, String cursor, int limit);

  CustomerResponse getCustomerFull(String tenantId, Long id);

  CustomerResponse updateCustomer(String tenantId, Long id, UpdateCustomerRequest req);

  void blockCustomer(String tenantId, Long id);

  void unblockCustomer(String tenantId, Long id);

  void addAddress(String tenantId, Long customerId, AddAddressRequest req);

  void removeAddress(String tenantId, Long customerId, Long addressId);
}
