package com.chamrong.iecommerce.promotion.domain.rule;

import static org.junit.jupiter.api.Assertions.*;

import com.chamrong.iecommerce.promotion.domain.model.PromotionRule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RetailSpecificationsTest {

  @Test
  void productInListShouldReturnTrueWhenMatch() {
    RetailSpecifications.ProductInListSpecification spec =
        new RetailSpecifications.ProductInListSpecification();
    PromotionRule rule = Mockito.mock(PromotionRule.class);
    Mockito.when(rule.getRuleData()).thenReturn("SKU1,SKU2");

    PromotionContext context =
        PromotionContext.builder()
            .items(
                List.of(
                    PromotionContext.CartItem.builder().productId("SKU1").build(),
                    PromotionContext.CartItem.builder().productId("SKU9").build()))
            .build();

    assertTrue(spec.isSatisfiedBy(rule, context));
  }

  @Test
  void minPurchaseQuantityShouldFailWhenUnderLimit() {
    RetailSpecifications.MinPurchaseQuantitySpecification spec =
        new RetailSpecifications.MinPurchaseQuantitySpecification();
    PromotionRule rule = Mockito.mock(PromotionRule.class);
    Mockito.when(rule.getRuleData()).thenReturn("5");

    PromotionContext context =
        PromotionContext.builder()
            .items(
                List.of(
                    PromotionContext.CartItem.builder().quantity(2).build(),
                    PromotionContext.CartItem.builder().quantity(2).build()))
            .build();

    assertFalse(spec.isSatisfiedBy(rule, context));
  }
}
