package com.chamrong.iecommerce.invoice.infrastructure.generator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * JPA entity backing the per-tenant invoice number counter.
 *
 * <p>Composite PK: (tenant_id, year). Holds the last issued sequence number for that tenant+year
 * combination. Rows are locked with PESSIMISTIC_WRITE by {@link DbInvoiceNumberGenerator}.
 */
@Entity
@Table(name = "invoice_tenant_counter")
@IdClass(InvoiceTenantCounter.PK.class)
public class InvoiceTenantCounter {

  @Id
  @Column(name = "tenant_id", nullable = false, length = 100)
  private String tenantId;

  @Id
  @Column(nullable = false)
  private int year;

  /** Short alphabetic prefix derived from tenant name (max 10 chars). */
  @Column(nullable = false, length = 10)
  private String prefix;

  @Column(name = "last_seq", nullable = false)
  private long lastSeq = 0L;

  protected InvoiceTenantCounter() {}

  /**
   * Creates a new counter for a tenant+year, deriving a prefix from the tenantId.
   *
   * @param tenantId the tenant
   * @param year the calendar year
   */
  public static InvoiceTenantCounter create(String tenantId, int year) {
    InvoiceTenantCounter c = new InvoiceTenantCounter();
    c.tenantId = tenantId;
    c.year = year;
    c.prefix = derivePrefix(tenantId);
    c.lastSeq = 0L;
    return c;
  }

  void increment() {
    this.lastSeq++;
  }

  public String getTenantId() {
    return tenantId;
  }

  public int getYear() {
    return year;
  }

  public String getPrefix() {
    return prefix;
  }

  public long getLastSeq() {
    return lastSeq;
  }

  private static String derivePrefix(String tenantId) {
    String cleaned = tenantId.toUpperCase().replaceAll("[^A-Z0-9]", "");
    return cleaned.length() > 10 ? cleaned.substring(0, 10) : cleaned;
  }

  /** Composite primary key class. */
  public static class PK implements Serializable {
    private String tenantId;
    private int year;

    public PK() {}

    public PK(String tenantId, int year) {
      this.tenantId = tenantId;
      this.year = year;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PK pk)) return false;
      return year == pk.year && java.util.Objects.equals(tenantId, pk.tenantId);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(tenantId, year);
    }
  }
}
