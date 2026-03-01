package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa.repository;

import com.chamrong.iecommerce.payment.infrastructure.persistence.jpa.entity.FinancialLedgerEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link FinancialLedgerEntity}.
 *
 * <p>Lives in infrastructure — NOT in the domain package.
 */
@Repository
public interface SpringDataFinancialLedgerRepository
    extends JpaRepository<FinancialLedgerEntity, Long> {

  List<FinancialLedgerEntity> findByStatus(FinancialLedgerEntity.LedgerStatus status);

  List<FinancialLedgerEntity> findByOrderId(Long orderId);

  List<FinancialLedgerEntity> findByDestinationUserId(Long userId);
}
