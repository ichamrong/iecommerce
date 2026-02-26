package com.chamrong.iecommerce.sale.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Represents a return/refund for a sale. Known as a Credit Note in accounting. */
@Entity
@Table(name = "sales_returns")
@Getter
@Setter
public class SaleReturn extends BaseTenantEntity {

  @Column(nullable = false)
  private String orderId;

  @Column(nullable = false)
  private String reason;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "refund_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "refund_currency"))
  })
  private Money refundAmount;

  @Enumerated(EnumType.STRING)
  private ReturnStatus status;

  @OneToMany(mappedBy = "saleReturn", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReturnItem> items = new ArrayList<>();

  public enum ReturnStatus {
    PENDING,
    APPROVED,
    REFUNDED,
    REJECTED
  }
}
