package com.chamrong.iecommerce.catalog.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

  Optional<Collection> findByTenantIdAndSlug(String tenantId, String slug);

  boolean existsByTenantIdAndSlugAndIdNot(String tenantId, String slug, Long excludeId);

  List<Collection> findByTenantIdAndActiveTrue(String tenantId);
}
