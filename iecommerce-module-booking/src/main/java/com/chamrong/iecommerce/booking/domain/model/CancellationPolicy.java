package com.chamrong.iecommerce.booking.domain.model;

import com.chamrong.iecommerce.common.Money;
import java.time.Instant;
import java.util.Objects;

/**
 * Cancellation policy: free until cutoff, partial/full penalty after.
 *
 * @param freeCancellationUntil cutoff instant; cancel before = no penalty
 * @param penaltyPercent percent of total (0-100) after cutoff
 * @param penaltyAmount fixed amount override (optional)
 */
public record CancellationPolicy(
    Instant freeCancellationUntil, int penaltyPercent, Money penaltyAmount) {

  public CancellationPolicy {
    penaltyPercent = Math.max(0, Math.min(100, penaltyPercent));
    penaltyAmount = penaltyAmount != null ? penaltyAmount : Money.zero("USD");
  }

  /**
   * Computes refund amount given total paid and cancellation time.
   *
   * @param totalPaid amount already paid
   * @param cancelledAt when cancellation occurs
   * @return refund amount (positive)
   */
  public Money computeRefund(Money totalPaid, Instant cancelledAt) {
    Objects.requireNonNull(totalPaid, "totalPaid");
    Objects.requireNonNull(cancelledAt, "cancelledAt");
    if (totalPaid.isZero()) return totalPaid;
    if (freeCancellationUntil != null && cancelledAt.isBefore(freeCancellationUntil)) {
      return totalPaid;
    }
    if (penaltyPercent >= 100) return Money.zero(totalPaid.getCurrency());
    if (!penaltyAmount.isZero()) {
      return totalPaid.subtract(penaltyAmount);
    }
    Money penalty =
        totalPaid.multiply(
            java.math.BigDecimal.valueOf(penaltyPercent).divide(java.math.BigDecimal.valueOf(100)));
    return totalPaid.subtract(penalty);
  }
}
