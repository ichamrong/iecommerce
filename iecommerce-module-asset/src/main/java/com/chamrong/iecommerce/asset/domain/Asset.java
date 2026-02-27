package com.chamrong.iecommerce.asset.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

  @Column(name = "is_public", nullable = false)
  private boolean isPublic = false;

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

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Version private Integer version;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Map<String, Object> metadata;
}
