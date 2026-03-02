package com.chamrong.iecommerce.review.domain.model;

/**
 * Value object representing a 1..5 star rating.
 *
 * <p>All validation is encapsulated here so higher layers cannot construct invalid ratings.
 */
public final class Rating {

  private final int value;

  private Rating(int value) {
    if (value < 1 || value > 5) {
      throw new IllegalArgumentException("Rating must be between 1 and 5");
    }
    this.value = value;
  }

  public static Rating of(int value) {
    return new Rating(value);
  }

  public int getValue() {
    return value;
  }
}
