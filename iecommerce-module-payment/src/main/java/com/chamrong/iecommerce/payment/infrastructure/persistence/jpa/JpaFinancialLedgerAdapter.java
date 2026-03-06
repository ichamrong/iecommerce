package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.payment.domain.FinancialLedgerEntry;
import com.chamrong.iecommerce.payment.domain.ports.FinancialLedgerPort;
import com.chamrong.iecommerce.payment.infrastructure.persistence.jpa.entity.FinancialLedgerEntity;
import com.chamrong.iecommerce.payment.infrastructure.persistence.jpa.entity.FinancialLedgerEntity.LedgerCategory;
import com.chamrong.iecommerce.payment.infrastructure.persistence.jpa.entity.FinancialLedgerEntity.LedgerType;
import com.chamrong.iecommerce.payment.infrastructure.persistence.jpa.repository.SpringDataFinancialLedgerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaFinancialLedgerAdapter implements FinancialLedgerPort {

  private final SpringDataFinancialLedgerRepository repository;

  @Override
  public void record(FinancialLedgerEntry entry) {
    repository.save(toEntity(entry));
  }

  @Override
  public void recordAll(List<FinancialLedgerEntry> entries) {
    repository.saveAll(entries.stream().map(this::toEntity).toList());
  }

  private FinancialLedgerEntity toEntity(FinancialLedgerEntry entry) {
    LedgerType ledgerType =
        switch (entry.getType()) {
          case CREDIT -> LedgerType.PAYOUT;
          case DEBIT -> LedgerType.REFUND;
        };
    return FinancialLedgerEntity.of(
        entry.getTenantId(),
        entry.getOrderId(),
        ledgerType,
        LedgerCategory.SERVICE,
        entry.getAmount());
  }
}
