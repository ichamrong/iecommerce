package com.chamrong.iecommerce.customer.infrastructure.persistence;

import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.ports.CustomerRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCustomerRepositoryAdapter implements CustomerRepositoryPort {

  private final JpaCustomerRepository jpaRepo;
  private final CustomerKeysetQuery keysetQuery;

  @Override
  public Customer save(Customer customer) {
    return jpaRepo.save(customer);
  }

  @Override
  public Optional<Customer> findById(Long id) {
    return jpaRepo.findById(id);
  }

  @Override
  public Optional<Customer> findByTenantIdAndEmail(String tenantId, String email) {
    return jpaRepo.findByTenantIdAndEmail(tenantId, email);
  }

  @Override
  public Optional<Customer> findByAuthUserId(Long authUserId) {
    return jpaRepo.findByAuthUserId(authUserId);
  }

  @Override
  public List<Customer> findCursorPage(
      String tenantId, Instant afterCreatedAt, Long afterId, int limit) {
    return keysetQuery.findNextPage(tenantId, afterCreatedAt, afterId, limit);
  }

  @Override
  public List<Customer> findAllByTenantId(String tenantId) {
    return jpaRepo.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId);
  }
}
