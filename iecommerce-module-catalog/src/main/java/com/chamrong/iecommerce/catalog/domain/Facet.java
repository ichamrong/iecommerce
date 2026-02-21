package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * Dynamic filtering dimension — e.g., "Brand", "Material", "Color".
 *
 * <p>The {@code code} (e.g., {@code "brand"}) is the locale-invariant machine key used in API
 * filters and URLs. The human-readable {@code name} lives in {@link FacetTranslation}.
 */
@Entity
@Table(
    name = "catalog_facets",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_facet_tenant_code",
            columnNames = {"tenant_id", "code"}))
public class Facet extends BaseTenantEntity {

  /** Machine-readable identifier — locale-invariant. E.g., {@code "brand"}. */
  @Getter
  @Column(nullable = false, length = 100)
  private String code;

  /** Whether this facet appears in the storefront filter panel. */
  @Getter
  @Setter
  @Column(name = "is_filterable", nullable = false)
  private boolean filterable = true;

  @OneToMany(mappedBy = "facet", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FacetTranslation> translations = new ArrayList<>();

  @OneToMany(mappedBy = "facet", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FacetValue> values = new ArrayList<>();

  protected Facet() {}

  public Facet(String tenantId, String code) {
    setTenantId(tenantId);
    this.code = code;
  }

  public void upsertTranslation(String locale, String name) {
    translationFor(locale)
        .ifPresentOrElse(
            t -> t.setName(name), () -> translations.add(new FacetTranslation(this, locale, name)));
  }

  public Optional<FacetTranslation> translationFor(String locale) {
    return translations.stream().filter(t -> t.getLocale().equals(locale)).findFirst();
  }

  /** Adds a new value; throws if code already exists for this facet. */
  public FacetValue addValue(String valueCode) {
    boolean duplicate = values.stream().anyMatch(v -> v.getCode().equals(valueCode));
    if (duplicate) {
      throw new IllegalArgumentException("FacetValue code already exists: " + valueCode);
    }
    FacetValue value = new FacetValue(this, valueCode);
    values.add(value);
    return value;
  }

  public void removeValue(Long valueId) {
    values.removeIf(v -> v.getId().equals(valueId));
  }

  public List<FacetTranslation> getTranslations() {
    return Collections.unmodifiableList(translations);
  }

  public List<FacetValue> getValues() {
    return Collections.unmodifiableList(values);
  }
}
