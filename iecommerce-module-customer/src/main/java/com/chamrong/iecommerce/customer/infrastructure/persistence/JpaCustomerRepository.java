package com.chamrong.iecommerce.customer.infrastructure.persistence;

import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link CustomerRepository} port. */
@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, Long>, CustomerRepository {
  @Override
  Optional<Customer> findByEmail(String email);

  @Override
  Optional<Customer> findByAuthUserId(Long authUserId);
}
