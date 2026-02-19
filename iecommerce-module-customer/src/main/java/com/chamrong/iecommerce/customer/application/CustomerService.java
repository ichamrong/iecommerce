package com.chamrong.iecommerce.customer.application;

import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

  private final CustomerRepository customerRepository;

  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @Transactional
  public Customer registerCustomer(Customer customer) {
    return customerRepository.save(customer);
  }

  public Optional<Customer> getCustomerById(Long id) {
    return customerRepository.findById(id);
  }

  public List<Customer> getAllCustomers() {
    return customerRepository.findAll();
  }
}
