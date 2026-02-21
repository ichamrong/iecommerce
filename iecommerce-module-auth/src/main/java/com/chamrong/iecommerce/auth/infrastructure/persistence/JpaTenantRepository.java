package com.chamrong.iecommerce.auth.infrastructure.persistence;

import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA adapter for the domain {@link TenantRepository} port. */
@Repository
public interface JpaTenantRepository extends JpaRepository<Tenant, Long>, TenantRepository {}
