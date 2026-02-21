package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

/**
 * A group of products, commonly called a "Collection" in e-commerce.
 *
 * <p>A Collection can be manual (products explicitly mapped) or automatic (products match a rule
 * like "tag=sale and price<20").
 */
@Entity
@Table(
    name = "catalog_collections",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_collection_tenant_slug",
            columnNames = {"tenant_id", "slug"}))
@SQLDelete(sql = "UPDATE catalog_collections SET deleted = TRUE, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = FALSE")
public class Collection extends BaseTenantEntity {

  @Getter
  @Setter
  @Column(nullable = false, length = 255)
  private String slug;

  @Getter
  @Setter
  @Column(name = "is_automatic", nullable = false)
  private boolean automatic = false;

  /** JSON-based rule representing conditions. Example: {"tag": "sale", "price_less_than": 50}. */
  @Getter
  @Setter
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String rule;

  @Getter
  @Setter
  @Column(nullable = false)
  private int sortOrder = 0;

  @Getter
  @Setter
  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CollectionTranslation> translations = new ArrayList<>();

  /** Manual grouping of products. */
  @ManyToMany
  @JoinTable(
      name = "catalog_collection_products",
      joinColumns = @JoinColumn(name = "collection_id"),
      inverseJoinColumns = @JoinColumn(name = "product_id"))
  private List<Product> products = new ArrayList<>();

  protected Collection() {}

  public Collection(String tenantId, String slug) {
    setTenantId(tenantId);
    this.slug = slug;
  }

  // ── Translation management ─────────────────────────────────────────────────

  public void upsertTranslation(String locale, String name, String description) {
    translationFor(locale)
        .ifPresentOrElse(
            t -> t.update(name, description),
            () -> translations.add(new CollectionTranslation(this, locale, name, description)));
  }

  public Optional<CollectionTranslation> translationFor(String locale) {
    return translations.stream().filter(t -> t.getLocale().equals(locale)).findFirst();
  }

  // ── Getters & Setters ──────────────────────────────────────────────────────

  public List<CollectionTranslation> getTranslations() {
    return Collections.unmodifiableList(translations);
  }

  public List<Product> getProducts() {
    return Collections.unmodifiableList(products);
  }

  public void addProduct(Product product) {
    if (!products.contains(product)) {
      products.add(product);
    }
  }

  public void removeProduct(Product product) {
    products.remove(product);
  }
}
