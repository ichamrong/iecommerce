package com.chamrong.iecommerce.sale.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sales_quotations")
@Getter
@Setter
public class Quotation extends BaseTenantEntity {

  @Column(nullable = false)
  private String customerId;

  private Instant expiryDate;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
  })
  private Money totalAmount;

  @Enumerated(EnumType.STRING)
  private QuotationStatus status;

  @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<QuotationItem> items = new ArrayList<>();

  public enum QuotationStatus {
    DRAFT,
    SENT,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    CONVERTED
  }
}
