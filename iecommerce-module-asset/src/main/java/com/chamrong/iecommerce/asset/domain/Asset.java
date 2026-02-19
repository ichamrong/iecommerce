package com.chamrong.iecommerce.asset.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "ecommerce_asset")
public class Asset extends BaseEntity {

  private String name;
  private String fileName;
  private String mimeType;
  private Long fileSize;
  private String source; // Storage provider path or local path

  @Enumerated(EnumType.STRING)
  private AssetType type;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public AssetType getType() {
    return type;
  }

  public void setType(AssetType type) {
    this.type = type;
  }
}
