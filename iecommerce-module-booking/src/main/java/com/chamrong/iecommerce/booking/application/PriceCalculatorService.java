package com.chamrong.iecommerce.booking.application;

import com.chamrong.iecommerce.common.Money;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Calculates the exact pricing for a stay, taking into account the base price, length of stay, and
 * dynamically evaluating active LodgeDiscountRules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCalculatorService {

  /**
   * Evaluates pricing day-by-day considering active discounts.
   *
   * @param resourceProductId The LODGE being booked
   * @param basePrice The original base price per night
   * @param checkIn Stay check-in date
   * @param checkOut Stay check-out date
   * @param customerId The guest booking the stay (for loyalty/VIP discounts)
   * @return A breakdown of pricing or merely the final discounted subtotal
   */
  public Money calculateNightlyPrice(
      Long resourceProductId,
      Money basePrice,
      LocalDate checkIn,
      LocalDate checkOut,
      Long customerId) {
    // 1. Fetch Approved LodgeVersion's discounts
    // 2. Filter applicable discounts based on customer status, dates, and days in advance
    // 3. Iterate night-by-night
    // 4. Apply Stacking Logic (Combine stackable rules sequentially)
    // 5. Apply Priority Logic (For non-stackable, winner takes all with highest priority/value)
    // 6. Respect MAX_DISCOUNT_PERCENT cap

    // Stub implementation returning the base price multiplied by duration until fully integrated
    long nights = checkIn.until(checkOut, java.time.temporal.ChronoUnit.DAYS);
    if (nights <= 0) {
      throw new IllegalArgumentException("Check-out must be after check-in.");
    }

    java.math.BigDecimal subTotal =
        basePrice.getAmount().multiply(java.math.BigDecimal.valueOf(nights));

    // Simulate fetching Active discounts via an event-driven or delegated proxy to Promotion Module
    // List<DiscountRuleDto> rules = promotionService.getActiveRules(resourceProductId);

    // Step 2: Filter by Customer Eligibility (e.g. VIP Tiers)
    // List<DiscountRuleDto> eligibleRules = filterByEligibility(rules, customerId, nights,
    // checkIn);

    // Step 3: Priority sorting and stacking
    // BigDecimal finalDiscount = BigDecimal.ZERO;
    // for (DiscountRuleDto rule : eligibleRules) {
    //    if (!rule.isStackable() && (finalDiscount.compareTo(BigDecimal.ZERO) > 0)) {
    //        continue; // Non-stackable rule skipped if a higher priority one was already applied
    //    }
    //    BigDecimal discountVal = (rule.getValueType() == ValueType.PERCENT)
    //        ? subTotal.multiply(BigDecimal.valueOf(rule.getValue() / 100.0))
    //        : BigDecimal.valueOf(rule.getValue());
    //
    //    finalDiscount = finalDiscount.add(discountVal);
    //    if (!rule.isStackable()) break;
    // }

    // Step 4: Cap Max Discount (e.g., cannot exceed 100%)
    // finalDiscount = finalDiscount.min(subTotal);

    // subTotal = subTotal.subtract(finalDiscount);

    return new Money(subTotal, basePrice.getCurrency());
  }
}
