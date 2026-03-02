package com.chamrong.iecommerce.booking.domain.model;

import com.chamrong.iecommerce.common.Money;
import java.util.List;

/**
 * Pricing breakdown: base rate, taxes, fees, discounts. Immutable.
 *
 * @param baseAmount base rate total
 * @param taxAmount tax total
 * @param feeAmount fees total
 * @param discountAmount discount total
 * @param total final total
 * @param lineItems optional breakdown items
 */
public record PricingBreakdown(
    Money baseAmount,
    Money taxAmount,
    Money feeAmount,
    Money discountAmount,
    Money total,
    List<PricingLineItem> lineItems) {

  public static PricingBreakdown of(Money total) {
    return new PricingBreakdown(
        total,
        Money.zero(total.getCurrency()),
        Money.zero(total.getCurrency()),
        Money.zero(total.getCurrency()),
        total,
        List.of());
  }

  public record PricingLineItem(String description, Money amount, String type) {}
}
