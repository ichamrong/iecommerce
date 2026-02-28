package com.chamrong.iecommerce.inventory.infrastructure.persistence;

import com.chamrong.iecommerce.inventory.domain.IdempotencyKey;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA backing repository for {@link IdempotencyKey}. */
@Repository
interface SpringDataIdempotencyRepository extends JpaRepository<IdempotencyKey, Long> {

  @Query(
      "SELECT k FROM IdempotencyKey k WHERE k.tenantId = :tenantId"
          + " AND k.operationType = :operationType AND k.referenceId = :referenceId")
  Optional<IdempotencyKey> findByKey(
      @Param("tenantId") String tenantId,
      @Param("operationType") String operationType,
      @Param("referenceId") String referenceId);
}
