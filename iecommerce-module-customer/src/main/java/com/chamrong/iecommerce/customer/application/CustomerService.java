package com.chamrong.iecommerce.customer.application;

import com.chamrong.iecommerce.customer.CustomerApi;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import java.util.Optional;
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
}
