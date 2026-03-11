package com.chamrong.iecommerce.invoice.infrastructure.persistence.jpa;

import com.chamrong.iecommerce.invoice.domain.Invoice;
import com.chamrong.iecommerce.invoice.domain.InvoiceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA interface for the {@link Invoice} entity. */
@Repository
public interface SpringDataInvoiceRepository extends JpaRepository<Invoice, Long> {

  Optional<Invoice> findByIdAndTenantId(Long id, String tenantId);

  Optional<Invoice> findByInvoiceNumberAndTenantId(String invoiceNumber, String tenantId);

  boolean existsByTenantIdAndInvoiceNumber(String tenantId, String invoiceNumber);

  List<Invoice> findByOrderId(Long orderId);

  @Query(
      "SELECT i FROM Invoice i "
          + "WHERE i.tenantId = :tenantId "
          + "AND (:status IS NULL OR i.status = :status) "
          + "ORDER BY i.issueDate DESC NULLS LAST, i.id DESC")
  List<Invoice> findFirstPage(
      @Param("tenantId") String tenantId,
      @Param("status") InvoiceStatus status,
      org.springframework.data.domain.Pageable pageable);

  @Query(
      "SELECT i FROM Invoice i "
          + "WHERE i.tenantId = :tenantId "
          + "AND (:status IS NULL OR i.status = :status) "
          + "AND (i.issueDate < :afterIssuedAt "
          + "     OR (i.issueDate = :afterIssuedAt AND i.id < :afterId)) "
          + "ORDER BY i.issueDate DESC NULLS LAST, i.id DESC")
  List<Invoice> findNextPage(
      @Param("tenantId") String tenantId,
      @Param("status") InvoiceStatus status,
      @Param("afterIssuedAt") Instant afterIssuedAt,
      @Param("afterId") Long afterId,
      org.springframework.data.domain.Pageable pageable);

  /** First page of cursor list across all tenants (platform admin only), no status filter. */
  @Query("SELECT i FROM Invoice i " + "ORDER BY i.issueDate DESC NULLS LAST, i.id DESC")
  List<Invoice> findFirstPageAllTenants(org.springframework.data.domain.Pageable pageable);

  /** First page of cursor list across all tenants with status filter. */
  @Query(
      "SELECT i FROM Invoice i "
          + "WHERE i.status = :status "
          + "ORDER BY i.issueDate DESC NULLS LAST, i.id DESC")
  List<Invoice> findFirstPageAllTenantsByStatus(
      @Param("status") InvoiceStatus status, org.springframework.data.domain.Pageable pageable);

  /** Next page across all tenants, no status filter. */
  @Query(
      "SELECT i FROM Invoice i "
          + "WHERE (i.issueDate < :afterIssuedAt "
          + "     OR (i.issueDate = :afterIssuedAt AND i.id < :afterId)) "
          + "ORDER BY i.issueDate DESC NULLS LAST, i.id DESC")
  List<Invoice> findNextPageAllTenants(
      @Param("afterIssuedAt") Instant afterIssuedAt,
      @Param("afterId") Long afterId,
      org.springframework.data.domain.Pageable pageable);

  /** Next page across all tenants with status filter. */
  @Query(
      "SELECT i FROM Invoice i "
          + "WHERE i.status = :status "
          + "AND (i.issueDate < :afterIssuedAt "
          + "     OR (i.issueDate = :afterIssuedAt AND i.id < :afterId)) "
          + "ORDER BY i.issueDate DESC NULLS LAST, i.id DESC")
  List<Invoice> findNextPageAllTenantsByStatus(
      @Param("status") InvoiceStatus status,
      @Param("afterIssuedAt") Instant afterIssuedAt,
      @Param("afterId") Long afterId,
      org.springframework.data.domain.Pageable pageable);
}
