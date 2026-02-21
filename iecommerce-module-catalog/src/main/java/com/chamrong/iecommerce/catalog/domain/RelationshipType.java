package com.chamrong.iecommerce.catalog.domain;

/** Type of merchandising relationship between two products. */
public enum RelationshipType {

  /** Higher-value alternative to the current product. */
  UPSELL,

  /** Complementary product often bought together. */
  CROSS_SELL,

  /** Product in the same category or style. */
  RELATED,

  /** Product that is bundled/packaged with the current product. */
  BUNDLE
}
