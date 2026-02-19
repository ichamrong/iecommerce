package com.chamrong.iecommerce.invoice.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
public class Invoice extends BaseTenantEntity {

  @Column(unique = true, nullable = false)
  private String invoiceNumber;

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false)
  private Instant invoiceDate = Instant.now();

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money totalAmount;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "invoice_id")
  private List<InvoiceLine> lines = new ArrayList<>();

  public String getInvoiceNumber() {
    return invoiceNumber;
  }

  public void setInvoiceNumber(String invoiceNumber) {
    this.invoiceNumber = invoiceNumber;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Instant getInvoiceDate() {
    return invoiceDate;
  }

  public void setInvoiceDate(Instant invoiceDate) {
    this.invoiceDate = invoiceDate;
  }

  public Money getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(Money totalAmount) {
    this.totalAmount = totalAmount;
  }

  public List<InvoiceLine> getLines() {
    return lines;
  }

  public void setLines(List<InvoiceLine> lines) {
    this.lines = lines;
  }
}
