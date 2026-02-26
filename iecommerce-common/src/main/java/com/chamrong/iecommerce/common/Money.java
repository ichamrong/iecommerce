package com.chamrong.iecommerce.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value Object representing an amount of money in a specific currency.
 *
 * <p><b>Banking/Insurance standard:</b>
 *
 * <ul>
 *   <li>All fields are effectively final — no setters, no mutation after construction.
 *   <li>All arithmetic ({@link #add}, {@link #subtract}) returns a <em>new</em> {@code Money}
 *       instance. This is the classic Value Object (Fowler) pattern used in financial systems.
 *   <li>All arithmetic uses {@link RoundingMode#HALF_EVEN} (banker's rounding) — the IEEE 754
 *       default used by financial institutions to minimize cumulative rounding errors.
 *   <li>Currency consistency is enforced on every arithmetic operation — adding USD to EUR throws
 *       an exception rather than silently producing a meaningless result.
 *   <li>Negative amounts are rejected in the factory method — money is unsigned in our domain.
 *   <li>The ISO 4217 currency code is validated via {@link Currency#getInstance(String)}.
 * </ul>
 */
@Embeddable
public final class Money {

  /** Scale used for all stored monetary values (matches accounting/SQL DECIMAL(19,4) standard). */
  public static final int SCALE = 4;

  /** Rounding mode used for all arithmetic — same as IEEE 754-2019 "roundTiesToEven". */
  public static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

  @Column(name = "amount")
  private final BigDecimal amount;

  @Column(name = "currency", length = 3)
  private final String currency;

  /** Required by JPA — package-private to prevent accidental use. */
  protected Money() {
    this.amount = BigDecimal.ZERO;
    this.currency = "USD";
  }

  /**
   * Primary constructor. Validates and normalizes inputs.
   *
   * @param amount the monetary value — must be non-null and {@code >= 0}
   * @param currency the ISO 4217 currency code — validated via {@link Currency}
   */
  public Money(final BigDecimal amount, final String currency) {
    Objects.requireNonNull(amount, "Money amount must not be null");
    Objects.requireNonNull(currency, "Money currency must not be null");
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Money amount must not be negative: " + amount);
    }
    try {
      Currency.getInstance(currency.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid ISO 4217 currency code: '" + currency + "'");
    }
    this.amount = amount.setScale(SCALE, ROUNDING);
    this.currency = currency.trim().toUpperCase();
  }

  // ── Factory methods ───────────────────────────────────────────────────────

  public static Money of(final BigDecimal amount, final String currency) {
    return new Money(amount, currency);
  }

  public static Money of(final String amount, final String currency) {
    return new Money(new BigDecimal(amount), currency);
  }

  public static Money zero(final String currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  // ── Value arithmetic (returns new instances — immutable pattern) ─────────

  /**
   * Returns a new Money equal to {@code this + other}.
   *
   * @throws IllegalArgumentException if currencies differ
   */
  public Money add(final Money other) {
    assertSameCurrency(other);
    return new Money(this.amount.add(other.amount).setScale(SCALE, ROUNDING), this.currency);
  }

  /**
   * Returns a new Money equal to {@code this - other}, floored at zero.
   *
   * @throws IllegalArgumentException if currencies differ
   */
  public Money subtract(final Money other) {
    assertSameCurrency(other);
    final BigDecimal result = this.amount.subtract(other.amount).setScale(SCALE, ROUNDING);
    return new Money(result.max(BigDecimal.ZERO), this.currency);
  }

  /** Returns a new Money scaled by a multiplier (e.g., quantity × unit price). */
  public Money multiply(final int factor) {
    if (factor < 0) {
      throw new IllegalArgumentException("Multiplication factor must be non-negative: " + factor);
    }
    return new Money(
        this.amount.multiply(BigDecimal.valueOf(factor)).setScale(SCALE, ROUNDING), this.currency);
  }

  /** Returns a new Money scaled by a BigDecimal (e.g., for tax-rate application). */
  public Money multiply(final BigDecimal factor) {
    Objects.requireNonNull(factor, "factor must not be null");
    return new Money(this.amount.multiply(factor).setScale(SCALE, ROUNDING), this.currency);
  }

  public boolean isGreaterThan(final Money other) {
    assertSameCurrency(other);
    return this.amount.compareTo(other.amount) > 0;
  }

  public boolean isZero() {
    return this.amount.compareTo(BigDecimal.ZERO) == 0;
  }

  // ── Accessors ─────────────────────────────────────────────────────────────

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  // ── Standard object methods ───────────────────────────────────────────────

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof final Money other)) return false;
    return Objects.equals(amount, other.amount) && Objects.equals(currency, other.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }

  @Override
  public String toString() {
    return amount.toPlainString() + " " + currency;
  }

  // ── Private helpers ────────────────────────────────────────────────────────

  private void assertSameCurrency(final Money other) {
    Objects.requireNonNull(other, "The other Money must not be null");
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: cannot operate on " + this.currency + " and " + other.currency);
    }
  }
}
