package com.chamrong.iecommerce.report.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Reporting money value object using minor units only (e.g. cents).
 *
 * <p>Immutable and deterministic; arithmetic is done in {@code long} to avoid rounding drift.
 */
public final class Money {

  private final long amountMinor;
  private final String currency;

  public Money(long amountMinor, String currency) {
    if (currency == null || currency.isBlank()) {
      throw new IllegalArgumentException("currency must not be blank");
    }
    this.amountMinor = amountMinor;
    this.currency = currency;
  }

  public static Money zero(String currency) {
    return new Money(0L, currency);
  }

  public long amountMinor() {
    return amountMinor;
  }

  public String currency() {
    return currency;
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(Math.addExact(this.amountMinor, other.amountMinor), currency);
  }

  public Money subtract(Money other) {
    requireSameCurrency(other);
    return new Money(Math.subtractExact(this.amountMinor, other.amountMinor), currency);
  }

  public Money max(Money other) {
    requireSameCurrency(other);
    return this.amountMinor >= other.amountMinor ? this : other;
  }

  public BigDecimal toMajorUnits() {
    return BigDecimal.valueOf(amountMinor, 2).setScale(2, RoundingMode.HALF_UP);
  }

  private void requireSameCurrency(Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: " + currency + " vs " + other.currency);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Money money)) return false;
    return amountMinor == money.amountMinor && currency.equals(money.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amountMinor, currency);
  }

  @Override
  public String toString() {
    return currency + " " + toMajorUnits();
  }
}
