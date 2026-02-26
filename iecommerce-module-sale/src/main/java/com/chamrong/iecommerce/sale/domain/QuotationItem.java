package com.chamrong.iecommerce.sale.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sales_quotation_items")
@Getter
@Setter
public class QuotationItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quotation_id")
  private Quotation quotation;

  @Column(nullable = false)
  private String productId;

  @Column(nullable = false)
  private BigDecimal quantity;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
  })
  private Money unitPrice;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "total_price_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "total_price_currency"))
  })
  private Money totalPrice;
}
