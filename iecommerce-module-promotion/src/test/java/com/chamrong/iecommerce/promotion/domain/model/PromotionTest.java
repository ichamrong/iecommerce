package com.chamrong.iecommerce.promotion.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class PromotionTest {

  @Test
  void shouldCreatePromotionWithDraftStatus() {
    Promotion promotion =
        Promotion.create(
            "T1",
            "Autumn Sale",
            "FALL20",
            PromotionType.PERCENTAGE,
            BigDecimal.valueOf(20.0),
            "FALL20",
            Instant.now(),
            Instant.now().plus(7, ChronoUnit.DAYS),
            1,
            false,
            100);

    assertEquals(PromotionStatus.DRAFT, promotion.getStatus());
    assertEquals(0, promotion.getUsedCount());
  }

  @Test
  void shouldTransitionStatusCorrectly() {
    Promotion promotion =
        Promotion.create(
            "T1",
            "Test",
            "TEST",
            PromotionType.PERCENTAGE,
            BigDecimal.valueOf(10.0),
            "TEST",
            Instant.now(),
            Instant.now().plus(1, ChronoUnit.DAYS),
            1,
            false,
            10);

    promotion.activate();
    assertEquals(PromotionStatus.ACTIVE, promotion.getStatus());

    promotion.pause();
    assertEquals(PromotionStatus.PAUSED, promotion.getStatus());

    promotion.archive();
    assertEquals(PromotionStatus.ARCHIVED, promotion.getStatus());
  }

  @Test
  void shouldTrackUsageAndEnforceLimits() {
    Promotion promotion =
        Promotion.create(
            "T1",
            "Limit Test",
            "LIMIT",
            PromotionType.FIXED_AMOUNT,
            BigDecimal.valueOf(50.0),
            "LIMIT",
            Instant.now(),
            Instant.now().plus(1, ChronoUnit.DAYS),
            1,
            false,
            2);

    promotion.activate();
    promotion.recordRedemption();
    assertEquals(1, promotion.getUsedCount());

    promotion.recordRedemption();
    assertEquals(2, promotion.getUsedCount());
  }
}
