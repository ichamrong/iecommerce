package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import com.chamrong.iecommerce.common.Money;
import jakarta.persistence.*;

@Entity
@Table(name = "catalog_product_variant")
public class ProductVariant extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  private String sku;
  private String name;

  @Embedded private Money price;

  private Integer stockLevel = 0;
  private boolean enabled = true;

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Money getPrice() {
    return price;
  }

  public void setPrice(Money price) {
    this.price = price;
  }

  public Integer getStockLevel() {
    return stockLevel;
  }

  public void setStockLevel(Integer stockLevel) {
    this.stockLevel = stockLevel;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
