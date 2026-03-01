package com.chamrong.iecommerce.payment.domain.ports;

import com.chamrong.iecommerce.payment.domain.FinancialLedgerEntry;
import java.util.List;

public interface FinancialLedgerPort {
  void record(FinancialLedgerEntry entry);

  void recordAll(List<FinancialLedgerEntry> entries);
}
