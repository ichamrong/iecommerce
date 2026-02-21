package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Aggregate root for a product in the catalog.
 *
 * <p>Only locale-invariant fields live here. All translatable text (name, description,
 * short_description, meta_title, meta_description) is stored in {@link ProductTranslation} keyed by
 * {@code (id, locale)}.
 *
 * <p>Lifecycle: DRAFT → ACTIVE → ARCHIVED (see {@link ProductStatus}).
 */
@Entity
@Table(
    name = "catalog_products",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_product_tenant_slug",
            columnNames = {"tenant_id", "slug"}))
@SQLDelete(sql = "UPDATE catalog_products SET deleted = TRUE, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = FALSE")
public class Product extends BaseTenantEntity {

  // Setters for non-state fields (allowed to be updated directly)
  @Getter
  @Setter
  @Column(nullable = false, length = 255)
  private String slug;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProductStatus status = ProductStatus.DRAFT;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProductType productType;

  @Getter
  @Setter
  @Column(name = "category_id")
  private Long categoryId;

  @Getter
  @Setter
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "base_price_amount")),
    @AttributeOverride(
        name = "currency",
        column = @Column(name = "base_price_currency", length = 3))
  })
  private Money basePrice;

  @Getter
  @Setter
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "compare_at_price_amount")),
    @AttributeOverride(
        name = "currency",
        column = @Column(name = "compare_at_price_currency", length = 3))
  })
  private Money compareAtPrice;

  @Getter
  @Setter
  @Column(length = 50)
  private String taxCategory = "STANDARD";

  /** Comma-separated tags for search/filtering — locale-invariant. */
  @Getter
  @Setter
  @Column(columnDefinition = "TEXT")
  private String tags;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductTranslation> translations = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductVariant> variants = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductAttribute> attributes = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductRelationship> relationships = new ArrayList<>();

  protected Product() {}

  public Product(String tenantId, String slug, ProductType productType, Money basePrice) {
    setTenantId(tenantId);
    this.slug = slug;
    this.productType = productType;
    this.basePrice = basePrice;
    this.status = ProductStatus.DRAFT;
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  /**
   * Transitions product from DRAFT or ARCHIVED → ACTIVE. Requires at least one enabled variant and
   * one translation.
   */
  public void publish() {
    if (translations.isEmpty()) {
      throw new IllegalStateException("Cannot publish a product with no translations.");
    }
    boolean hasEnabledVariant = variants.stream().anyMatch(ProductVariant::isEnabled);
    if (!hasEnabledVariant) {
      throw new IllegalStateException("Cannot publish a product with no enabled variants.");
    }
    this.status = ProductStatus.ACTIVE;
  }

  /** Transitions ACTIVE → ARCHIVED. */
  public void archive() {
    if (this.status != ProductStatus.ACTIVE) {
      throw new IllegalStateException("Only ACTIVE products can be archived.");
    }
    this.status = ProductStatus.ARCHIVED;
  }

  /** Transitions ARCHIVED → ACTIVE. */
  public void reactivate() {
    if (this.status != ProductStatus.ARCHIVED) {
      throw new IllegalStateException("Only ARCHIVED products can be reactivated.");
    }
    this.status = ProductStatus.ACTIVE;
  }

  // ── Translation management ─────────────────────────────────────────────────

  /**
   * Creates or updates the translation for the given locale. The {@code locale} value is immutable
   * once a translation row exists.
   */
  public void upsertTranslation(
      String locale,
      String name,
      String description,
      String shortDescription,
      String metaTitle,
      String metaDescription) {
    translationFor(locale)
        .ifPresentOrElse(
            t -> t.update(name, description, shortDescription, metaTitle, metaDescription),
            () ->
                translations.add(
                    new ProductTranslation(
                        this,
                        locale,
                        name,
                        description,
                        shortDescription,
                        metaTitle,
                        metaDescription)));
  }

  /** Returns the translation for the given locale, or empty if not available. */
  public Optional<ProductTranslation> translationFor(String locale) {
    return translations.stream().filter(t -> t.getLocale().equals(locale)).findFirst();
  }

  /** Removes the translation for the given locale (orphanRemoval handles DB delete). */
  public void removeTranslation(String locale) {
    translations.removeIf(t -> t.getLocale().equals(locale));
  }

  // ── Variant management ─────────────────────────────────────────────────────

  /** Adds a new variant; throws if SKU already exists on this product. */
  public ProductVariant addVariant(String sku, Money price) {
    boolean duplicate = variants.stream().anyMatch(v -> v.getSku().equals(sku));
    if (duplicate) {
      throw new IllegalArgumentException("SKU already exists on this product: " + sku);
    }
    ProductVariant variant = new ProductVariant(this, sku, price);
    variants.add(variant);
    return variant;
  }

  // ── Getters (no public setters for state fields — use named methods) ───────

  public List<ProductTranslation> getTranslations() {
    return Collections.unmodifiableList(translations);
  }

  public List<ProductVariant> getVariants() {
    return Collections.unmodifiableList(variants);
  }

  public List<ProductAttribute> getAttributes() {
    return Collections.unmodifiableList(attributes);
  }

  public List<ProductRelationship> getRelationships() {
    return Collections.unmodifiableList(relationships);
  }
}
