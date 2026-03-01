package com.chamrong.iecommerce.payment.domain.ledger;

import java.util.List;

/**
 * Outbound port (Repository) for the financial ledger.
 *
 * <p>Implementations live in {@code infrastructure/persistence/jpa/adapter/}. Must NOT contain JPA
 * or Spring annotations.
 */
public interface LedgerRepository {

  /**
   * Records a single ledger entry.
   *
   * @param entry the entry to persist; must not be null
   */
  void record(FinancialLedgerEntry entry);

  /**
   * Records multiple ledger entries atomically (batch insert).
   *
   * @param entries the entries to persist; must not be null or empty
   */
  void recordAll(List<FinancialLedgerEntry> entries);
}
