package com.chamrong.iecommerce.promotion.domain.rule;

import com.chamrong.iecommerce.promotion.domain.model.PromotionRule;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

/** Specifications for Retail product-based rules. */
@Component
public class RetailSpecifications {

  @Component
  public static class ProductInListSpecification implements PromotionSpecification {
    @Override
    public boolean isSatisfiedBy(PromotionRule rule, PromotionContext context) {
      if (context.getItems() == null || context.getItems().isEmpty()) return false;
      List<String> allowedIds = Arrays.asList(rule.getRuleData().split(","));
      return context.getItems().stream()
          .anyMatch(
              item -> item.getProductId() != null && allowedIds.contains(item.getProductId()));
    }
  }

  @Component
  public static class CategoryMatchSpecification implements PromotionSpecification {
    @Override
    public boolean isSatisfiedBy(PromotionRule rule, PromotionContext context) {
      if (context.getItems() == null || context.getItems().isEmpty()) return false;
      String category = rule.getRuleData().trim();
      return context.getItems().stream()
          .anyMatch(
              item -> item.getCategory() != null && category.equalsIgnoreCase(item.getCategory()));
    }
  }

  @Component
  public static class MinPurchaseQuantitySpecification implements PromotionSpecification {
    @Override
    public boolean isSatisfiedBy(PromotionRule rule, PromotionContext context) {
      if (context.getItems() == null) return false;
      int minQty = Integer.parseInt(rule.getRuleData());
      int totalQty =
          context.getItems().stream().mapToInt(PromotionContext.CartItem::getQuantity).sum();
      return totalQty >= minQty;
    }
  }
}
