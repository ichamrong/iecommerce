package com.chamrong.iecommerce.customer.application.query;

import com.chamrong.iecommerce.customer.application.dto.CustomerResponse;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerQueryHandler {

  private final CustomerRepository customerRepository;
  private final com.chamrong.iecommerce.customer.application.CustomerMapper mapper;

  public CustomerQueryHandler(
      CustomerRepository customerRepository,
      com.chamrong.iecommerce.customer.application.CustomerMapper mapper) {
    this.customerRepository = customerRepository;
    this.mapper = mapper;
  }

  public CustomerResponse findById(Long id) {
    return customerRepository
        .findById(id)
        .map(mapper::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
  }

  public CustomerResponse findByAuthUserId(Long authUserId) {
    return customerRepository
        .findByAuthUserId(authUserId)
        .map(mapper::toResponse)
        .orElseThrow(
            () ->
                new EntityNotFoundException("Customer not found for auth user id: " + authUserId));
  }

  public List<CustomerResponse> findAll() {
    return customerRepository.findAll().stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }
}
