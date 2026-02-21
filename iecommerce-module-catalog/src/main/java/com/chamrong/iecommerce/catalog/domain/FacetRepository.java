package com.chamrong.iecommerce.catalog.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacetRepository extends JpaRepository<Facet, Long> {

  Optional<Facet> findByTenantIdAndCode(String tenantId, String code);

  List<Facet> findByTenantId(String tenantId);

  List<Facet> findByTenantIdAndFilterableTrue(String tenantId);

  boolean existsByTenantIdAndCode(String tenantId, String code);
}
