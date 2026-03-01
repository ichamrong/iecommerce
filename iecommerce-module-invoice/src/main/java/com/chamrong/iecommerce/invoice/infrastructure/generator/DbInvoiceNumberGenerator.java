package com.chamrong.iecommerce.invoice.infrastructure.generator;

import com.chamrong.iecommerce.invoice.domain.ports.InvoiceNumberGeneratorPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates unique, sequential, tenant-scoped invoice numbers using a counter table with
 * pessimistic row-level locking (SELECT ... FOR UPDATE).
 *
 * <p>Format: {@code {PREFIX}-{YYYY}-{SEQ:06d}} — e.g., {@code ACME-2026-000042}.
 *
 * <p>Runs in its own transaction ({@link Propagation#REQUIRES_NEW}) so the counter increment is
 * committed immediately, even if the outer transaction rolls back. This prevents sequence gaps from
 * being re-used in the rollback scenario, ensuring strict monotonicity with no duplicates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbInvoiceNumberGenerator implements InvoiceNumberGeneratorPort {

  private final EntityManager em;

  /**
   * {@inheritDoc}
   *
   * <p>Uses a pessimistic write lock on the {@code invoice_tenant_counter} row to prevent
   * concurrent threads from receiving the same sequence number.
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public String next(String tenantId, int year) {
    InvoiceTenantCounter counter = findOrCreate(tenantId, year);
    counter.increment();
    em.flush();

    String number = String.format("%s-%d-%06d", counter.getPrefix(), year, counter.getLastSeq());
    log.debug("Generated invoice number: {} for tenant={}", number, tenantId);
    return number;
  }

  private InvoiceTenantCounter findOrCreate(String tenantId, int year) {
    try {
      return em.createQuery(
              "SELECT c FROM InvoiceTenantCounter c WHERE c.tenantId = :tid AND c.year = :yr",
              InvoiceTenantCounter.class)
          .setParameter("tid", tenantId)
          .setParameter("yr", year)
          .setLockMode(LockModeType.PESSIMISTIC_WRITE)
          .getSingleResult();
    } catch (NoResultException e) {
      InvoiceTenantCounter counter = InvoiceTenantCounter.create(tenantId, year);
      em.persist(counter);
      em.flush();
      // Re-fetch with lock to handle concurrent first-row creation
      return em.createQuery(
              "SELECT c FROM InvoiceTenantCounter c WHERE c.tenantId = :tid AND c.year = :yr",
              InvoiceTenantCounter.class)
          .setParameter("tid", tenantId)
          .setParameter("yr", year)
          .setLockMode(LockModeType.PESSIMISTIC_WRITE)
          .getSingleResult();
    }
  }
}
