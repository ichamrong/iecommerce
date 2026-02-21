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

/** Locale-specific display name for a {@link Facet}. E.g., "Brand" (en) → "ម៉ាក" (km). */
@Entity
@Table(
    name = "catalog_facet_translations",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_facet_translation_locale",
            columnNames = {"facet_id", "locale"}))
public class FacetTranslation {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facet_id", nullable = false, updatable = false)
  private Facet facet;

  @Getter
  @Column(nullable = false, length = 10, updatable = false)
  private String locale;

  @Getter
  @Setter
  @Column(nullable = false, length = 255)
  private String name;

  protected FacetTranslation() {}

  public FacetTranslation(Facet facet, String locale, String name) {
    this.facet = facet;
    this.locale = locale;
    this.name = name;
  }
}
