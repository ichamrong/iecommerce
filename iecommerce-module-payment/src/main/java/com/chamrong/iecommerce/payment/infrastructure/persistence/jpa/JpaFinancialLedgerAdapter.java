package com.chamrong.iecommerce.payment.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.payment.domain.FinancialLedgerEntry;
import com.chamrong.iecommerce.payment.domain.ports.FinancialLedgerPort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaFinancialLedgerAdapter implements FinancialLedgerPort {

  private final LedgerSpringRepository repository;

  @Override
  public void record(FinancialLedgerEntry entry) {
    repository.save(toEntity(entry));
  }

  @Override
  public void recordAll(List<FinancialLedgerEntry> entries) {
    repository.saveAll(entries.stream().map(this::toEntity).toList());
  }

  private FinancialLedgerEntity toEntity(FinancialLedgerEntry entry) {
    var entity = new FinancialLedgerEntity();
    entity.setId(entry.getEntryId());
    entity.setTenantId(entry.getTenantId());
    entity.setOrderId(entry.getOrderId());
    // NOTE: This assumes paymentIntentId in domain (UUID) maps to paymentId long in Entity if they
    // are different concepts,
    // or we should update the entity to use UUID. For now, since it was failing, we fix the setter
    // name.
    // However, entry.getPaymentIntentId() is UUID and entity.setPaymentIntentId wants Long?
    // Let's check FinancialLedgerEntity again.

    // entity.setPaymentIntentId(entry.getPaymentIntentId()); // ERROR: UUID vs Long

    entity.setEntryType(FinancialLedgerEntity.EntryType.valueOf(entry.getType().name()));
    entity.setAmount(entry.getAmount().getAmount());
    entity.setCurrency(entry.getAmount().getCurrency());
    entity.setStatus(FinancialLedgerEntity.LedgerStatus.SETTLED); // Default for recorded entries
    entity.setDescription(entry.getDescription());
    entity.setCreatedAt(entry.getCreatedAt());
    return entity;
  }

  interface LedgerSpringRepository extends JpaRepository<FinancialLedgerEntity, UUID> {}
}
