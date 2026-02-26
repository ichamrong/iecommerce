package com.chamrong.iecommerce.payment.application;

import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.payment.domain.FinancialLedger;
import com.chamrong.iecommerce.payment.domain.FinancialLedgerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialLedgerService {

  private final FinancialLedgerRepository ledgerRepository;

  @Transactional
  public FinancialLedger createLedgerEntry(
      String tenantId,
      Long orderId,
      Long destinationUserId,
      FinancialLedger.LedgerType type,
      FinancialLedger.LedgerCategory category,
      Money amount) {
    FinancialLedger ledger = new FinancialLedger();
    ledger.setTenantId(tenantId);
    ledger.setOrderId(orderId);
    ledger.setDestinationUserId(destinationUserId);
    ledger.setType(type);
    ledger.setCategory(category);
    ledger.setAmount(amount);
    ledger.setStatus(FinancialLedger.LedgerStatus.PENDING);
    return ledgerRepository.save(ledger);
  }

  @Transactional(readOnly = true)
  public List<FinancialLedger> getPendingPayouts() {
    return ledgerRepository.findByStatus(FinancialLedger.LedgerStatus.PENDING);
  }

  @Transactional
  public FinancialLedger executePayout(
      Long ledgerId, String adminReferenceId, String bankTransactionId) {
    FinancialLedger ledger =
        ledgerRepository
            .findById(ledgerId)
            .orElseThrow(() -> new IllegalArgumentException("Ledger not found"));

    if (ledger.getStatus() != FinancialLedger.LedgerStatus.PENDING) {
      throw new IllegalStateException("Can only execute PENDING ledgers");
    }

    ledger.setStatus(FinancialLedger.LedgerStatus.EXECUTED);
    ledger.setAdminReferenceId(adminReferenceId);
    ledger.setBankTransactionId(bankTransactionId);

    log.info("Ledger entry executed, id={}, bankTxId={}", ledgerId, bankTransactionId);
    return ledgerRepository.save(ledger);
  }
}
