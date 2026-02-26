package com.chamrong.iecommerce.payment.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialLedgerRepository extends JpaRepository<FinancialLedger, Long> {
  List<FinancialLedger> findByStatus(FinancialLedger.LedgerStatus status);

  List<FinancialLedger> findByOrderId(Long orderId);

  List<FinancialLedger> findByDestinationUserId(Long userId);
}
