package com.chamrong.iecommerce.customer;

import java.util.Optional;

/** Public API of the Customer module. */
public interface CustomerApi {

  /** Returns a customer by ID. */
  Optional<CustomerInfo> getCustomer(Long id);
}
