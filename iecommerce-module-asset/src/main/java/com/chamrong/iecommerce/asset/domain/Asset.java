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
@Table(
    name = "ecommerce_asset",
    indexes = {
      @jakarta.persistence.Index(
          name = "idx_asset_tenant_parent",
          columnList = "tenant_id, parent_id"),
      @jakarta.persistence.Index(name = "idx_asset_tenant_type", columnList = "tenant_id, type"),
      @jakarta.persistence.Index(name = "idx_asset_tenant_path", columnList = "tenant_id, path")
    })
public class Asset extends BaseTenantEntity {

  @Column(nullable = false, length = 255)
  private String name;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(length = 100)
  private String mimeType;

  @Column(nullable = false)
  private Long fileSize = 0L;

  /** Storage provider path, URL, or S3/MinIO object key. Null for virtual folders. */
  @Column(columnDefinition = "TEXT")
  private String source;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AssetType type;

  @jakarta.annotation.Nullable
  @Column(length = 255)
  private String path; // Virtual absolute path, e.g., "products/shoes", "invoices/2024"

  @jakarta.annotation.Nullable
  @Column(name = "parent_id")
  private Long parentId; // For recursive virtual folders

  @Column(name = "is_folder", nullable = false)
  private boolean isFolder = false;
}
