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

/** Locale-specific display label for a {@link FacetValue}. E.g., "Apple" (en) → "អាប់ផ្លោ" (km). */
@Entity
@Table(
    name = "catalog_facet_value_translations",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_facet_value_translation_locale",
            columnNames = {"facet_value_id", "locale"}))
public class FacetValueTranslation {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facet_value_id", nullable = false, updatable = false)
  private FacetValue facetValue;

  @Getter
  @Column(nullable = false, length = 10, updatable = false)
  private String locale;

  @Setter
  @Getter
  @Column(nullable = false, length = 255)
  private String value;

  protected FacetValueTranslation() {}

  public FacetValueTranslation(FacetValue facetValue, String locale, String value) {
    this.facetValue = facetValue;
    this.locale = locale;
    this.value = value;
  }
}
