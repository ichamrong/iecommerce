package com.chamrong.iecommerce.customer.infrastructure;

import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCustomerRepository implements CustomerRepository {

  private final CustomerJpaInterface jpaInterface;

  public JpaCustomerRepository(CustomerJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Customer save(Customer customer) {
    return jpaInterface.save(customer);
  }

  @Override
  public Optional<Customer> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public Optional<Customer> findByEmail(String email) {
    return jpaInterface.findByEmail(email);
  }

  @Override
  public Optional<Customer> findByAuthUserId(Long authUserId) {
    return jpaInterface.findByAuthUserId(authUserId);
  }

  @Override
  public List<Customer> findAll() {
    return jpaInterface.findAll();
  }

  public interface CustomerJpaInterface extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByAuthUserId(Long authUserId);
  }
}
