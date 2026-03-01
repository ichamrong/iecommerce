package com.chamrong.iecommerce.promotion.domain.rule;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Contextual data required for promotion rule evaluation. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionContext {
  private String tenantId;
  private Money baseAmount;
  private Instant evaluationTime;

  // Business Specific Context
  @Builder.Default private List<CartItem> items = new ArrayList<>();
  private String region;
  private String locationId;
  private String deliveryType;
  private Map<String, Object> attributes;

  public PromotionContext(String tenantId, Map<String, Object> attributes) {
    this(tenantId, attributes, Instant.now());
  }

  public PromotionContext(String tenantId, Map<String, Object> attributes, Instant evaluationTime) {
    this.tenantId = tenantId;
    this.attributes = attributes != null ? attributes : new HashMap<>();
    this.evaluationTime = evaluationTime != null ? evaluationTime : Instant.now();
    this.items = new ArrayList<>();
  }

  public static PromotionContext fromAttributes(String tenantId, Map<String, Object> attributes) {
    return new PromotionContext(tenantId, attributes);
  }

  // Manual getters to avoid Lombok issues
  public String getTenantId() {
    return tenantId;
  }

  public Money getBaseAmount() {
    return baseAmount;
  }

  public Instant getEvaluationTime() {
    return evaluationTime;
  }

  public List<CartItem> getItems() {
    return items != null ? items : Collections.emptyList();
  }

  public Map<String, Object> getAttributes() {
    return attributes != null ? attributes : Collections.emptyMap();
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CartItem {
    private String productId;
    private String category;
    private int quantity;
    private Money price;

    public String getProductId() {
      return productId;
    }

    public String getCategory() {
      return category;
    }

    public int getQuantity() {
      return quantity;
    }

    public Money getPrice() {
      return price;
    }
  }
}
