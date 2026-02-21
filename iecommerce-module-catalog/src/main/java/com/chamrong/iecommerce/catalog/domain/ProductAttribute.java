package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Static technical specification of a {@link Product} — locale-invariant. */
@Getter
@Entity
@Table(name = "catalog_product_attributes")
public class ProductAttribute extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false, updatable = false)
  private Product product;

  /** E.g., "Screen Size", "Battery Capacity". */
  @Column(name = "attribute_key", nullable = false, length = 255)
  private String key;

  /** E.g., "6.7", "5000". */
  @Setter
  @Column(name = "attribute_value", nullable = false, length = 500)
  private String value;

  /** E.g., "inches", "mAh". Null if unitless. */
  @Setter
  @Column(length = 50)
  private String unit;

  @Setter
  @Column(nullable = false)
  private int sortOrder = 0;

  protected ProductAttribute() {}

  public ProductAttribute(Product product, String key, String value) {
    this.product = product;
    this.key = key;
    this.value = value;
  }
}
