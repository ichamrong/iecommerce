package com.chamrong.iecommerce.order;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

/**
 * DTO for order data used in reconciliation reports. Exposed via {@link OrderApi} so the report
 * module does not depend on order domain types.
 */
@Value
@Builder
public class OrderReconciliationItem {
  Long id;
  String code;
  BigDecimal totalAmount;
  String state;
}
