package com.chamrong.iecommerce.promotion.domain.rule;

import com.chamrong.iecommerce.promotion.domain.model.Promotion;
import com.chamrong.iecommerce.promotion.domain.model.PromotionType;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Domain service for calculating promotion discounts. */
@Service
@RequiredArgsConstructor
public class PromotionDiscountService {

  private final Map<PromotionType, DiscountStrategy> strategies = new ConcurrentHashMap<>();

  private final DiscountStrategies.PercentageDiscountStrategy percentageStrategy;
  private final DiscountStrategies.FixedAmountDiscountStrategy fixedAmountStrategy;

  @jakarta.annotation.PostConstruct
  public void init() {
    strategies.put(PromotionType.PERCENTAGE, percentageStrategy);
    strategies.put(PromotionType.FIXED_AMOUNT, fixedAmountStrategy);
  }

  public BigDecimal calculateDiscount(Promotion promotion, PromotionContext context) {
    DiscountStrategy strategy = strategies.get(promotion.getType());
    if (strategy == null || context.getBaseAmount() == null) {
      return BigDecimal.ZERO;
    }
    return strategy.calculate(context.getBaseAmount().getAmount(), promotion.getValue());
  }
}
