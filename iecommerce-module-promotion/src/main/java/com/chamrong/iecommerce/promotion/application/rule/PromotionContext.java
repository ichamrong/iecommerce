package com.chamrong.iecommerce.promotion.application.rule;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PromotionContext {
  private final String tenantId;
  private final Money baseAmount;
  private final Instant evaluationTime;

  // Business Specific Context
  private final List<CartItem> items;
  private final String region; // e.g., "Phnom Penh"
  private final String locationId; // e.g., "BRANCH_001"
  private final String deliveryType; // e.g., "PICKUP", "DELIVERY"
  private final Map<String, Object> attributes;

  public Map<String, Object> getAttributes() {
    return attributes != null ? attributes : Collections.emptyMap();
  }

  @Getter
  @Builder
  public static class CartItem {
    private final String productId;
    private final String category;
    private final int quantity;
    private final Money price;
  }
}
