package com.chamrong.iecommerce.auth.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.auth.infrastructure.persistence.jpa.entity.TenantEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataTenantRepository extends JpaRepository<TenantEntity, Long> {

  Optional<TenantEntity> findByCode(String code);

  boolean existsByCode(String code);
}
