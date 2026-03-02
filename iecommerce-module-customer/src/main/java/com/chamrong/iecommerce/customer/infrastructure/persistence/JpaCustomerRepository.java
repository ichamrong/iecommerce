package com.chamrong.iecommerce.customer.infrastructure.persistence;

import com.chamrong.iecommerce.customer.domain.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Customer}. Used by {@link JpaCustomerRepositoryAdapter}.
 */
@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, Long> {

  Optional<Customer> findByTenantIdAndEmail(String tenantId, String email);

  Optional<Customer> findByAuthUserId(Long authUserId);

  List<Customer> findAllByTenantIdOrderByCreatedAtDescIdDesc(String tenantId);
}
