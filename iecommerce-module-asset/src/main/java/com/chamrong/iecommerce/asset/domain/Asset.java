package com.chamrong.iecommerce.asset.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ecommerce_asset")
public class Asset extends BaseTenantEntity {

  @Column(nullable = false, length = 255)
  private String name;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false, length = 100)
  private String mimeType;

  @Column(nullable = false)
  private Long fileSize;

  /** Storage provider path, URL, or S3/MinIO object key. */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String source;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AssetType type;

  @Column(length = 255)
  private String path; // e.g., "products/shoes", "invoices/2024"
}
