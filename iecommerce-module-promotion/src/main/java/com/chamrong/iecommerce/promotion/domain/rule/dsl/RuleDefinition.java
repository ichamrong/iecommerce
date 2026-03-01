package com.chamrong.iecommerce.promotion.domain.rule.dsl;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Root object for a dynamic promotion rule definition. */
@Data
@NoArgsConstructor
public class RuleDefinition {
  private String schemaVersion = "v1";
  private Condition condition;
  private List<Action> actions;

  public Condition getCondition() {
    return condition;
  }

  public List<Action> getActions() {
    return actions;
  }

  public String getSchemaVersion() {
    return schemaVersion;
  }

  // ── Conditions ──────────────────────────────────────────────────────────

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = CartSubtotalCondition.class, name = "CART_SUBTOTAL"),
    @JsonSubTypes.Type(value = SkuInListCondition.class, name = "SKU_IN_LIST"),
    @JsonSubTypes.Type(value = CategoryInListCondition.class, name = "CATEGORY_IN_LIST"),
    @JsonSubTypes.Type(value = LogicalCondition.class, name = "LOGICAL")
  })
  public interface Condition {}

  @Data
  public static class CartSubtotalCondition implements Condition {
    private BigDecimal minAmount;
    private String operator = "GTE"; // GTE, GT

    public BigDecimal getMinAmount() {
      return minAmount;
    }

    public String getOperator() {
      return operator;
    }
  }

  @Data
  public static class SkuInListCondition implements Condition {
    private List<String> skus;
    private boolean negate = false;

    public List<String> getSkus() {
      return skus;
    }

    public boolean isNegate() {
      return negate;
    }
  }

  @Data
  public static class CategoryInListCondition implements Condition {
    private List<String> categories;
    private boolean negate = false;

    public List<String> getCategories() {
      return categories;
    }

    public boolean isNegate() {
      return negate;
    }
  }

  @Data
  public static class LogicalCondition implements Condition {
    private String operator; // AND, OR, NOT
    private List<Condition> conditions;

    public String getOperator() {
      return operator;
    }

    public List<Condition> getConditions() {
      return conditions;
    }
  }

  // ── Actions ─────────────────────────────────────────────────────────────

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = PercentageDiscountAction.class, name = "PERCENT_OFF"),
    @JsonSubTypes.Type(value = AmountDiscountAction.class, name = "AMOUNT_OFF"),
    @JsonSubTypes.Type(value = BogoAction.class, name = "BOGO")
  })
  public interface Action {}

  @Data
  public static class PercentageDiscountAction implements Action {
    private BigDecimal percentage;
    private BigDecimal maxCap; // Optional cap
    private String target = "CART"; // CART, ITEM, CATEGORY
    private List<String> targetIds;

    public BigDecimal getPercentage() {
      return percentage;
    }

    public BigDecimal getMaxCap() {
      return maxCap;
    }

    public String getTarget() {
      return target;
    }

    public List<String> getTargetIds() {
      return targetIds;
    }
  }

  @Data
  public static class AmountDiscountAction implements Action {
    private BigDecimal amount;
    private String target = "CART"; // CART, ITEM

    public BigDecimal getAmount() {
      return amount;
    }

    public String getTarget() {
      return target;
    }
  }

  @Data
  public static class BogoAction implements Action {
    private int buyQuantity;
    private int getQuantity;
    private String strategy = "CHEAPEST_FREE";
    private List<String> eligibleSkus;

    public int getBuyQuantity() {
      return buyQuantity;
    }

    public int getGetQuantity() {
      return getQuantity;
    }

    public String getStrategy() {
      return strategy;
    }

    public List<String> getEligibleSkus() {
      return eligibleSkus;
    }
  }
}
