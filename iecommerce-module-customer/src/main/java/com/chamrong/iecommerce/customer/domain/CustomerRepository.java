package com.chamrong.iecommerce.customer.domain;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
  Customer save(Customer customer);

  Optional<Customer> findById(Long id);

  Optional<Customer> findByEmail(String email);

  Optional<Customer> findByAuthUserId(Long authUserId);

  List<Customer> findAll();
}
