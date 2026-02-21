package com.chamrong.iecommerce.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Locale-specific name for a {@link ProductVariant}. E.g., "Red / XL" → "ក្រហម / XL" (km). */
@Entity
@Table(
    name = "catalog_product_variant_translations",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_variant_translation_locale",
            columnNames = {"variant_id", "locale"}))
public class ProductVariantTranslation {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id", nullable = false, updatable = false)
  private ProductVariant variant;

  @Getter
  @Column(nullable = false, length = 10, updatable = false)
  private String locale;

  @Getter
  @Setter
  @Column(nullable = false, length = 255)
  private String name;

  protected ProductVariantTranslation() {}

  public ProductVariantTranslation(ProductVariant variant, String locale, String name) {
    this.variant = variant;
    this.locale = locale;
    this.name = name;
  }
}
