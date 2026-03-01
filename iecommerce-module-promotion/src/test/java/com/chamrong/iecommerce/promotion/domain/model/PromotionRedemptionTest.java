package com.chamrong.iecommerce.promotion.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PromotionRedemptionTest {

  @Test
  void shouldCreateReservedRedemption() {
    Promotion p = Mockito.mock(Promotion.class);
    PromotionRedemption redemption =
        PromotionRedemption.reserve(p, "T1", "O1", "C1", "KEY-1", BigDecimal.valueOf(10.0));

    assertEquals(RedemptionStatus.RESERVED, redemption.getStatus());
    assertEquals("KEY-1", redemption.getRedemptionKey());
  }

  @Test
  void shouldApplyRedemption() {
    Promotion p = Mockito.mock(Promotion.class);
    PromotionRedemption redemption =
        PromotionRedemption.reserve(p, "T1", "O1", "C1", "KEY-1", BigDecimal.valueOf(10.0));

    redemption.apply();
    assertEquals(RedemptionStatus.APPLIED, redemption.getStatus());
    assertNotNull(redemption.getAppliedAt());
  }

  @Test
  void shouldNotApplyTwice() {
    Promotion p = Mockito.mock(Promotion.class);
    PromotionRedemption redemption =
        PromotionRedemption.reserve(p, "T1", "O1", "C1", "KEY-1", BigDecimal.valueOf(10.0));

    redemption.apply();
    assertThrows(RuntimeException.class, redemption::apply);
  }
}
