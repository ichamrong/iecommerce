package com.chamrong.iecommerce.asset.application.mapper;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.StorageConstants;

public final class AssetMapper {

  private AssetMapper() {
    // Utility class
  }

  public static AssetResponse toResponse(Asset a) {
    if (a == null) {
      return null;
    }
    String proxyUrl =
        StorageConstants.API_V1_ASSETS
            + StorageConstants.PATH_DELIMITER
            + a.getId()
            + StorageConstants.DOWNLOAD_SUFFIX;
    return new AssetResponse(
        a.getId(),
        a.getName(),
        a.getFileName(),
        a.getMimeType(),
        a.getFileSize(),
        a.getSource(),
        proxyUrl,
        a.getType() != null ? a.getType().name() : null,
        a.isPublic(),
        a.getCreatedAt());
  }
}
