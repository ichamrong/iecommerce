package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * A specific purchasable SKU of a {@link Product}.
 *
 * <p>Locale-invariant fields: sku, price, compareAtPrice, weight, stockLevel, enabled, sortOrder.
 * The variant {@code name} (e.g., "Red / XL") lives in {@link ProductVariantTranslation}.
 */
@Entity
@Table(
    name = "catalog_product_variants",
    uniqueConstraints = @UniqueConstraint(name = "uq_variant_sku", columnNames = "sku"))
@SQLDelete(
    sql = "UPDATE catalog_product_variants SET deleted = TRUE, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = FALSE")
public class ProductVariant extends BaseEntity {

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false, updatable = false)
  private Product product;

  @Getter
  @Column(nullable = false, length = 100)
  private String sku;

  @Getter
  @Setter
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "price_amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "price_currency", length = 3))
  })
  private Money price;

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

  /** Weight in grams — used for shipping calculation. */
  @Getter
  @Setter
  @Column(name = "weight_grams", precision = 10, scale = 3)
  private BigDecimal weightGrams;

  @Getter
  @Setter
  @Column(nullable = false)
  private int stockLevel = 0;

  @Getter
  @Setter
  @Column(nullable = false)
  private boolean enabled = true;

  @Getter
  @Setter
  @Column(nullable = false)
  private int sortOrder = 0;

  @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductVariantTranslation> translations = new ArrayList<>();

  protected ProductVariant() {}

  public ProductVariant(Product product, String sku, Money price) {
    this.product = product;
    this.sku = sku;
    this.price = price;
  }

  // ── Translation management ─────────────────────────────────────────────────

  public void upsertTranslation(String locale, String name) {
    translationFor(locale)
        .ifPresentOrElse(
            t -> t.setName(name),
            () -> translations.add(new ProductVariantTranslation(this, locale, name)));
  }

  public Optional<ProductVariantTranslation> translationFor(String locale) {
    return translations.stream().filter(t -> t.getLocale().equals(locale)).findFirst();
  }

  // ── Getters & setters ──────────────────────────────────────────────────────

  public List<ProductVariantTranslation> getTranslations() {
    return Collections.unmodifiableList(translations);
  }
}
