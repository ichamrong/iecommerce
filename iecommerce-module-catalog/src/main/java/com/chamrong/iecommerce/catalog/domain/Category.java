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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Hierarchical product category using Adjacency List + Materialized Path.
 *
 * <p>The {@code materializedPath} encodes the full ancestor chain (e.g., {@code /1/4/12/}),
 * enabling fast subtree queries: {@code WHERE materialized_path LIKE '/1/%'}.
 *
 * <p>Only structural fields live here. The {@code name} and {@code description} are locale-specific
 * and stored in {@link CategoryTranslation}.
 */
@Entity
@Table(
    name = "catalog_categories",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_category_tenant_slug",
            columnNames = {"tenant_id", "slug"}))
@SQLDelete(sql = "UPDATE catalog_categories SET deleted = TRUE, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = FALSE")
public class Category extends BaseTenantEntity {

  @Getter
  @Setter
  @Column(nullable = false, length = 255)
  private String slug;

  @Getter
  @Setter
  @Column(name = "parent_id")
  private Long parentId;

  /** Full ancestor path: {@code /1/4/12/} — recomputed on tree moves. */
  @Getter
  @Column(name = "materialized_path", length = 1000)
  private String materializedPath;

  @Getter
  @Column(nullable = false)
  private int depth = 0;

  @Getter
  @Setter
  @Column(nullable = false)
  private int sortOrder = 0;

  @Getter
  @Setter
  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Getter
  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CategoryTranslation> translations = new ArrayList<>();

  protected Category() {}

  public Category(String tenantId, String slug) {
    setTenantId(tenantId);
    this.slug = slug;
  }

  // ── Translation management ─────────────────────────────────────────────────

  public void upsertTranslation(String locale, String name, String description) {
    translationFor(locale)
        .ifPresentOrElse(
            t -> t.update(name, description),
            () -> translations.add(new CategoryTranslation(this, locale, name, description)));
  }

  public Optional<CategoryTranslation> translationFor(String locale) {
    return translations.stream().filter(t -> t.getLocale().equals(locale)).findFirst();
  }

  // ── Tree management ────────────────────────────────────────────────────────

  /**
   * Recomputes the materialized path after a tree move. Call this on the moved node AND all its
   * descendants.
   *
   * @param parentPath the parent's materialized path, or {@code ""} for root nodes
   */
  public void rebuildPath(String parentPath) {
    this.materializedPath = parentPath + getId() + "/";
    this.depth = (int) materializedPath.chars().filter(c -> c == '/').count() - 1;
  }

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }

  // ── Getters ────────────────────────────────────────────────────────────────

  public List<CategoryTranslation> getTranslations() {
    return Collections.unmodifiableList(translations);
  }
}
