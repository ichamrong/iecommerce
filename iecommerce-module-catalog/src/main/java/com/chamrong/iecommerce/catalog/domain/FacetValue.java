package com.chamrong.iecommerce.catalog.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

/**
 * A specific value within a {@link Facet}. E.g., Facet="Brand", Value code="apple".
 *
 * <p>The {@code code} is locale-invariant and used in URL filters (e.g., {@code ?brand=apple}). The
 * human label per language lives in {@link FacetValueTranslation}.
 */
@Entity
@Table(
    name = "catalog_facet_values",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_facet_value_code",
            columnNames = {"facet_id", "code"}))
public class FacetValue {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facet_id", nullable = false, updatable = false)
  private Facet facet;

  /** Locale-invariant machine key. E.g., {@code "apple"}, {@code "samsung"}. */
  @Getter
  @Column(nullable = false, length = 100, updatable = false)
  private String code;

  @OneToMany(mappedBy = "facetValue", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FacetValueTranslation> translations = new ArrayList<>();

  protected FacetValue() {}

  public FacetValue(Facet facet, String code) {
    this.facet = facet;
    this.code = code;
  }

  public void upsertTranslation(String locale, String value) {
    translationFor(locale)
        .ifPresentOrElse(
            t -> t.setValue(value),
            () -> translations.add(new FacetValueTranslation(this, locale, value)));
  }

  public Optional<FacetValueTranslation> translationFor(String locale) {
    return translations.stream().filter(t -> t.getLocale().equals(locale)).findFirst();
  }

  public List<FacetValueTranslation> getTranslations() {
    return Collections.unmodifiableList(translations);
  }
}
