package com.chamrong.iecommerce.promotion.domain.rule;

import com.chamrong.iecommerce.promotion.domain.model.RuleType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/** Automatically registers all available specifications into the evaluator. */
@Configuration
@RequiredArgsConstructor
public class PromotionRuleConfiguration {

  private final PromotionRuleEvaluator evaluator;

  // Retail
  private final RetailSpecifications.ProductInListSpecification productInListSpec;
  private final RetailSpecifications.CategoryMatchSpecification categoryMatchSpec;
  private final RetailSpecifications.MinPurchaseQuantitySpecification minQtySpec;

  // Hospitality
  private final HospitalitySpecifications.MinNightsStaySpecification minNightsSpec;
  private final HospitalitySpecifications.EarlyBirdSpecification earlyBirdSpec;

  @PostConstruct
  public void registerSpecs() {
    evaluator.register(RuleType.PRODUCT_IN_LIST, productInListSpec);
    evaluator.register(RuleType.CATEGORY_MATCH, categoryMatchSpec);
    evaluator.register(RuleType.MIN_PURCHASE_QUANTITY, minQtySpec);

    evaluator.register(RuleType.MIN_NIGHTS_STAY, minNightsSpec);
    evaluator.register(RuleType.EARLY_BIRD_DAYS, earlyBirdSpec);

    // General / Others to be added here
  }
}
