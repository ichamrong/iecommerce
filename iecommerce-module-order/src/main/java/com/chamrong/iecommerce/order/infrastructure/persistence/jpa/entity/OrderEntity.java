package com.chamrong.iecommerce.order.infrastructure.persistence.jpa.entity;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import com.chamrong.iecommerce.order.domain.OrderState;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity for Order. Domain model is {@link
 * com.chamrong.iecommerce.order.domain.Order}.
 */
@Entity
@Table(name = "ecommerce_order")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity extends BaseTenantEntity {

  @Version
  @Column(nullable = false)
  private Long version = 0L;

  @Column(length = 100, nullable = false, updatable = false)
  private String code;

  @Column private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private OrderState state = OrderState.AddingItems;

  @Column(columnDefinition = "TEXT")
  private String shippingAddress;

  @Column(name = "confirmed_at")
  private Instant confirmedAt;

  @Column(name = "cancelled_at")
  private Instant cancelledAt;

  @Column(name = "shipped_at")
  private Instant shippedAt;

  @Column(length = 100)
  private String trackingNumber;

  @Column(length = 50)
  private String voucherCode;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "discount_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "discount_currency", length = 3))
  })
  private Money discount;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItemEntity> items = new ArrayList<>();

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency"))
  })
  private Money subTotal;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
  })
  private Money total;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "deposit_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "deposit_currency"))
  })
  private Money depositAmount;
}
