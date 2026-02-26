package com.chamrong.iecommerce.promotion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromotionDomainTest {

  private Promotion promotion;

  @BeforeEach
  void setUp() {
    promotion = new Promotion();
  }

  @Test
  void testIsActiveAt_WhenActiveAndInDateRange() {
    promotion.setActive(true);
    promotion.setValidFrom(Instant.now().minus(1, ChronoUnit.DAYS));
    promotion.setValidTo(Instant.now().plus(1, ChronoUnit.DAYS));

    assertTrue(promotion.isActiveAt(Instant.now()));
  }

  @Test
  void testIsActiveAt_WhenNotActive() {
    promotion.setActive(false);
    promotion.setValidFrom(Instant.now().minus(1, ChronoUnit.DAYS));
    promotion.setValidTo(Instant.now().plus(1, ChronoUnit.DAYS));

    assertFalse(promotion.isActiveAt(Instant.now()));
  }

  @Test
  void testIsActiveAt_WhenBeforeValidFrom() {
    promotion.setActive(true);
    promotion.setValidFrom(Instant.now().plus(1, ChronoUnit.DAYS));

    assertFalse(promotion.isActiveAt(Instant.now()));
  }

  @Test
  void testIsActiveAt_WhenAfterValidTo() {
    promotion.setActive(true);
    promotion.setValidTo(Instant.now().minus(1, ChronoUnit.DAYS));

    assertFalse(promotion.isActiveAt(Instant.now()));
  }

  @Test
  void testCalculateDiscount_Percentage() {
    promotion.setType(PromotionType.PERCENTAGE);
    promotion.setValue(20.0); // 20% discount

    BigDecimal baseAmount = new BigDecimal("100.00");
    BigDecimal discount = promotion.calculateDiscount(baseAmount);

    assertEquals(new BigDecimal("20.0000"), discount);
  }

  @Test
  void testCalculateDiscount_PercentageCapped() {
    promotion.setType(PromotionType.PERCENTAGE);
    promotion.setValue(150.0); // 150% discount

    BigDecimal baseAmount = new BigDecimal("100.00");
    BigDecimal discount = promotion.calculateDiscount(baseAmount);

    assertEquals(new BigDecimal("100.0000"), discount.setScale(4, java.math.RoundingMode.HALF_UP));
  }

  @Test
  void testCalculateDiscount_FixedAmount() {
    promotion.setType(PromotionType.FIXED_AMOUNT);
    promotion.setValue(30.0); // $30 discount

    BigDecimal baseAmount = new BigDecimal("100.00");
    BigDecimal discount = promotion.calculateDiscount(baseAmount);

    assertEquals(new BigDecimal("30.0"), discount);
  }

  @Test
  void testCalculateDiscount_FixedAmountCapped() {
    promotion.setType(PromotionType.FIXED_AMOUNT);
    promotion.setValue(30.0); // $30 discount

    BigDecimal baseAmount = new BigDecimal("20.00");
    BigDecimal discount = promotion.calculateDiscount(baseAmount);

    assertEquals(new BigDecimal("20.00"), discount);
  }

  @Test
  void testCalculateDiscount_FreeShipping() {
    promotion.setType(PromotionType.FREE_SHIPPING);
    promotion.setValue(100.0); // value doesn't matter

    BigDecimal baseAmount = new BigDecimal("100.00");
    BigDecimal discount = promotion.calculateDiscount(baseAmount);

    assertEquals(BigDecimal.ZERO, discount);
  }
}
