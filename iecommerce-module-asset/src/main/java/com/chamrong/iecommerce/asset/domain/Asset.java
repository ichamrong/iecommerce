package com.chamrong.iecommerce.asset.domain;

import com.chamrong.iecommerce.common.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "ecommerce_asset",
    indexes = {
      @Index(name = "idx_asset_tenant_parent", columnList = "tenant_id, parent_id"),
      @Index(name = "idx_asset_tenant_type", columnList = "tenant_id, type"),
      @Index(name = "idx_asset_tenant_path", columnList = "tenant_id, path")
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

  // ── Domain behaviour ─────────────────────────────────────────────────────

  public void updateMetadata(Map<String, Object> newMetadata) {
    this.metadata = newMetadata;
  }

  public void updateSource(String newSource) {
    this.source = newSource;
  }

  public void markPublic() {
    this.isPublic = true;
  }

  public void markPrivate() {
    this.isPublic = false;
  }

  public void softDelete() {
    this.deletedAt = Instant.now();
  }

  // ── Behaviour (move/rename) ───────────────────────────────────────────────

  public void moveTo(String newSource, Long newParentId, String newPath) {
    this.source = newSource;
    this.parentId = newParentId;
    this.path = newPath;
  }

  public void rename(String newName, String newFileName) {
    this.name = newName;
    this.fileName = newFileName;
  }

  // ── Factories ─────────────────────────────────────────────────────────────

  public static Asset create(
      String tenantId,
      String name,
      String fileName,
      String mimeType,
      long fileSize,
      String source,
      AssetType type,
      String path,
      boolean isPublic) {
    var a = new Asset();
    a.setTenantId(tenantId);
    a.name = name;
    a.fileName = fileName;
    a.mimeType = mimeType;
    a.fileSize = fileSize;
    a.source = source;
    a.type = type;
    a.path = path;
    a.isPublic = isPublic;
    a.isFolder = false;
    return a;
  }

  public static Asset folder(
      String tenantId,
      String name,
      String source,
      AssetType type,
      Long parentId,
      String materializedPath,
      String mimeType) {
    var a = new Asset();
    a.setTenantId(tenantId);
    a.name = name;
    a.fileName = name;
    a.mimeType = mimeType;
    a.fileSize = 0L;
    a.source = source;
    a.type = type;
    a.parentId = parentId;
    a.isFolder = true;
    a.path = materializedPath;
    return a;
  }

  public static Asset copyOf(Asset source, String newSource, String copyName, Long targetParentId) {
    var a = new Asset();
    a.setTenantId(source.getTenantId());
    a.name = copyName;
    a.fileName = source.fileName;
    a.mimeType = source.mimeType;
    a.fileSize = source.fileSize;
    a.source = newSource;
    a.type = source.type;
    a.parentId = targetParentId;
    a.isFolder = false;
    return a;
  }
}
