package com.chamrong.iecommerce.promotion.application.rule;

import com.chamrong.iecommerce.promotion.domain.PromotionRule;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PromotionRuleChecker {

  public boolean isEligible(PromotionRule rule, PromotionContext context) {
    try {
      return switch (rule.getType()) {
        // Retail
        case PRODUCT_IN_LIST -> checkProductInList(rule, context);
        case CATEGORY_MATCH -> checkCategoryMatch(rule, context);
        case MIN_PURCHASE_QUANTITY -> checkMinQuantity(rule, context);

        // Hospitality
        case MIN_NIGHTS_STAY -> checkMinNights(rule, context);
        case EARLY_BIRD_DAYS -> checkEarlyBird(rule, context);

        // F&B / General
        case TIME_OF_DAY_RANGE -> checkTimeRange(rule, context);
        case DAY_OF_WEEK -> checkDayOfWeek(rule, context);

        // Geographic / Logistic
        case REGION_MATCH -> checkRegionMatch(rule, context);
        case DELIVERY_ZONE -> checkDeliveryZone(rule, context);
        case STORE_PICKUP_ONLY -> checkPickupOnly(rule, context);

        default -> true; // If rule unknown, assume it doesn't block
      };
    } catch (Exception e) {
      log.warn("Error evaluating rule {}: {}", rule.getType(), e.getMessage());
      return false;
    }
  }

  private boolean checkProductInList(PromotionRule rule, PromotionContext context) {
    List<String> allowedIds = Arrays.asList(rule.getRuleData().split(","));
    return context.getItems().stream().anyMatch(item -> allowedIds.contains(item.getProductId()));
  }

  private boolean checkCategoryMatch(PromotionRule rule, PromotionContext context) {
    String category = rule.getRuleData().trim();
    return context.getItems().stream()
        .anyMatch(item -> category.equalsIgnoreCase(item.getCategory()));
  }

  private boolean checkMinQuantity(PromotionRule rule, PromotionContext context) {
    int minQty = Integer.parseInt(rule.getRuleData());
    int totalQty =
        context.getItems().stream().mapToInt(PromotionContext.CartItem::getQuantity).sum();
    return totalQty >= minQty;
  }

  private boolean checkMinNights(PromotionRule rule, PromotionContext context) {
    int minNights = Integer.parseInt(rule.getRuleData());
    Object nights = context.getAttributes().get("nights");
    return nights != null && ((Number) nights).intValue() >= minNights;
  }

  private boolean checkEarlyBird(PromotionRule rule, PromotionContext context) {
    int minDays = Integer.parseInt(rule.getRuleData());
    Object daysAdvance = context.getAttributes().get("daysAdvance");
    return daysAdvance != null && ((Number) daysAdvance).intValue() >= minDays;
  }

  private boolean checkTimeRange(PromotionRule rule, PromotionContext context) {
    // ruleData e.g. "14:00-17:00"
    String[] parts = rule.getRuleData().split("-");
    LocalTime start = LocalTime.parse(parts[0]);
    LocalTime end = LocalTime.parse(parts[1]);
    LocalTime now = LocalTime.now(); // In production, use context.evaluationTime
    return !now.isBefore(start) && !now.isAfter(end);
  }

  private boolean checkDayOfWeek(PromotionRule rule, PromotionContext context) {
    // ruleData e.g. "MONDAY,WEDNESDAY"
    String day = java.time.LocalDate.now().getDayOfWeek().name();
    return rule.getRuleData().toUpperCase().contains(day);
  }

  private boolean checkRegionMatch(PromotionRule rule, PromotionContext context) {
    if (context.getRegion() == null) return false;
    List<String> allowedRegions = Arrays.asList(rule.getRuleData().split(","));
    return allowedRegions.stream().anyMatch(r -> r.trim().equalsIgnoreCase(context.getRegion()));
  }

  private boolean checkDeliveryZone(PromotionRule rule, PromotionContext context) {
    Object zone = context.getAttributes().get("deliveryZone");
    if (zone == null) return false;
    List<String> allowedZones = Arrays.asList(rule.getRuleData().split(","));
    return allowedZones.stream().anyMatch(z -> z.trim().equalsIgnoreCase(String.valueOf(zone)));
  }

  private boolean checkPickupOnly(PromotionRule rule, PromotionContext context) {
    return "PICKUP".equalsIgnoreCase(context.getDeliveryType());
  }
}
