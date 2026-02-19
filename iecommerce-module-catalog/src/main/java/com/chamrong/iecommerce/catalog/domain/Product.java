package com.chamrong.iecommerce.catalog.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "catalog_product")
public class Product extends BaseEntity {

  private String name;
  private String slug;
  private String description;
  private boolean enabled;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductVariant> variants = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public List<ProductVariant> getVariants() {
    return variants;
  }

  public void setVariants(List<ProductVariant> variants) {
    this.variants = variants;
  }
}
