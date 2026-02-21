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

/**
 * Stores locale-specific text fields for a {@link Product}.
 *
 * <p>The pair {@code (product_id, locale)} is unique — one row per locale per product. Translations
 * are managed via {@link Product#upsertTranslation}.
 *
 * <p>The {@code locale} field uses IETF BCP 47 tags: {@code en}, {@code km}, {@code zh}, etc.
 */
@Entity
@Table(
    name = "catalog_product_translations",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_product_translation_locale",
            columnNames = {"product_id", "locale"}))
public class ProductTranslation {

  // Getters — no public setters (locale is identity; text is updated via update())
  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false, updatable = false)
  private Product product;

  /** IETF BCP 47 locale tag — immutable once created. */
  @Getter
  @Column(nullable = false, length = 10, updatable = false)
  private String locale;

  @Getter
  @Column(nullable = false, length = 255)
  private String name;

  @Getter
  @Column(columnDefinition = "TEXT")
  private String description;

  @Getter
  @Column(length = 512)
  private String shortDescription;

  @Getter
  @Column(length = 255)
  private String metaTitle;

  @Getter
  @Column(length = 512)
  private String metaDescription;

  /** Required by JPA — not for external use. */
  protected ProductTranslation() {}

  public ProductTranslation(
      Product product,
      String locale,
      String name,
      String description,
      String shortDescription,
      String metaTitle,
      String metaDescription) {
    this.product = product;
    this.locale = locale;
    this.name = name;
    this.description = description;
    this.shortDescription = shortDescription;
    this.metaTitle = metaTitle;
    this.metaDescription = metaDescription;
  }

  /** Updates all mutable text fields. The {@code locale} identity cannot be changed. */
  public void update(
      String name,
      String description,
      String shortDescription,
      String metaTitle,
      String metaDescription) {
    this.name = name;
    this.description = description;
    this.shortDescription = shortDescription;
    this.metaTitle = metaTitle;
    this.metaDescription = metaDescription;
  }
}
