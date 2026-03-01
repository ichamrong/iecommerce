package com.chamrong.iecommerce.promotion.application.service;

import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Service for allocating a total discount amount across line items. */
@Service
public class AllocationService {

  /**
   * Allocates discount proportionally across items. sum(allocated) will always equal totalDiscount.
   */
  public Map<String, BigDecimal> allocate(
      BigDecimal totalDiscount, List<PromotionContext.CartItem> items) {
    BigDecimal totalSubtotal =
        items.stream()
            .map(i -> i.getPrice().getAmount().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    Map<String, BigDecimal> allocations = new HashMap<>();
    if (totalSubtotal.compareTo(BigDecimal.ZERO) == 0
        || totalDiscount.compareTo(BigDecimal.ZERO) == 0) {
      return allocations;
    }

    BigDecimal remainingDiscount = totalDiscount;
    for (int i = 0; i < items.size(); i++) {
      PromotionContext.CartItem item = items.get(i);
      BigDecimal itemSubtotal =
          item.getPrice().getAmount().multiply(BigDecimal.valueOf(item.getQuantity()));

      if (i == items.size() - 1) {
        // Last item gets the remainder to ensure sum(allocated) == totalDiscount
        allocations.put(item.getProductId(), remainingDiscount);
      } else {
        BigDecimal share =
            itemSubtotal.multiply(totalDiscount).divide(totalSubtotal, 4, RoundingMode.HALF_UP);
        allocations.put(item.getProductId(), share);
        remainingDiscount = remainingDiscount.subtract(share);
      }
    }
    return allocations;
  }
}
