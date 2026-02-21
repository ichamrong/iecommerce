package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Directed merchandising link between two products within the same tenant. */
@Getter
@Entity
@Table(
    name = "catalog_product_relationships",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_product_relationship",
            columnNames = {"product_id", "related_product_id", "type"}))
public class ProductRelationship extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false, updatable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "related_product_id", nullable = false, updatable = false)
  private Product relatedProduct;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RelationshipType type;

  @Setter
  @Column(nullable = false)
  private int sortOrder = 0;

  protected ProductRelationship() {}

  public ProductRelationship(Product product, Product relatedProduct, RelationshipType type) {
    this.product = product;
    this.relatedProduct = relatedProduct;
    this.type = type;
  }
}
