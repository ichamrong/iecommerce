package com.chamrong.iecommerce.customer.infrastructure.persistence.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for CustomerEntity. Keyset and filter queries in adapter. */
public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, Long> {

  @Query("SELECT c FROM CustomerEntity c WHERE c.tenantId = :tenantId AND c.email = :email")
  List<CustomerEntity> findByTenantIdAndEmail(
      @Param("tenantId") String tenantId, @Param("email") String email);

  @Query(
      "SELECT c FROM CustomerEntity c WHERE c.tenantId = :tenantId AND c.authUserId = :authUserId")
  List<CustomerEntity> findByTenantIdAndAuthUserId(
      @Param("tenantId") String tenantId, @Param("authUserId") Long authUserId);
}
