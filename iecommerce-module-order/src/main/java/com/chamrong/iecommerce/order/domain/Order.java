package com.chamrong.iecommerce.order.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ecommerce_order")
public class Order extends BaseEntity {

  private String code;

  @Enumerated(EnumType.STRING)
  private OrderState state = OrderState.AddingItems;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new ArrayList<>();

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

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public OrderState getState() {
    return state;
  }

  public void setState(OrderState state) {
    this.state = state;
  }

  public List<OrderItem> getItems() {
    return items;
  }

  public void setItems(List<OrderItem> items) {
    this.items = items;
  }

  public Money getSubTotal() {
    return subTotal;
  }

  public void setSubTotal(Money subTotal) {
    this.subTotal = subTotal;
  }

  public Money getTotal() {
    return total;
  }

  public void setTotal(Money total) {
    this.total = total;
  }
}
