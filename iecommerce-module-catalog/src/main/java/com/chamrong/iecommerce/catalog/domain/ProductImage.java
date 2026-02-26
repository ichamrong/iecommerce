package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Link between a Product and an Asset. One product can have multiple images (gallery). */
@Entity
@Table(name = "catalog_product_images")
@Getter
@Setter
@NoArgsConstructor
public class ProductImage extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  /** Foreign key to Asset in the asset module. */
  @Column(name = "asset_id", nullable = false)
  private Long assetId;

  @Column(nullable = false)
  private int sortOrder = 0;

  @Column private String altText;

  public ProductImage(Product product, Long assetId) {
    this.product = product;
    this.assetId = assetId;
  }
}
