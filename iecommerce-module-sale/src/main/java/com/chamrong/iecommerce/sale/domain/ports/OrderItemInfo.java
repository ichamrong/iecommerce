package com.chamrong.iecommerce.sale.domain.ports;

import com.chamrong.iecommerce.common.Money;
import java.math.BigDecimal;
import lombok.Value;

@Value
public class OrderItemInfo {
  Long id;
  Long orderId;
  String productId;
  BigDecimal quantity;
  Money unitPrice;
}
