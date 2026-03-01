package com.chamrong.iecommerce.promotion.domain.rule;

import com.chamrong.iecommerce.promotion.domain.model.PromotionRule;
import org.springframework.stereotype.Component;

/** Specifications for Hospitality/Accommodations rules. */
@Component
public class HospitalitySpecifications {

  @Component
  public static class MinNightsStaySpecification implements PromotionSpecification {
    @Override
    public boolean isSatisfiedBy(PromotionRule rule, PromotionContext context) {
      int minNights = Integer.parseInt(rule.getRuleData());
      Object nights = context.getAttributes().get("nights");
      return nights != null && ((Number) nights).intValue() >= minNights;
    }
  }

  @Component
  public static class EarlyBirdSpecification implements PromotionSpecification {
    @Override
    public boolean isSatisfiedBy(PromotionRule rule, PromotionContext context) {
      int minDays = Integer.parseInt(rule.getRuleData());
      Object daysAdvance = context.getAttributes().get("daysAdvance");
      return daysAdvance != null && ((Number) daysAdvance).intValue() >= minDays;
    }
  }
}
