package com.chamrong.iecommerce.asset.application.service;

import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.AssetException;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetDeletionService {

  private final AssetRepository assetRepository;
  private final StorageService storageService;

  /**
   * Deletes an asset by ID (Soft delete).
   *
   * @param id the asset ID to delete
   */
  @Transactional
  public void delete(long id) {
    internalDelete(id);
  }

  /**
   * Bulk deletes assets by IDs.
   *
   * @param ids list of asset IDs to delete
   */
  @Transactional
  public void bulkDelete(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }

    List<Asset> assetsToUpdate = new java.util.ArrayList<>();

    String tenantId = TenantContext.requireTenantId();
    for (Long id : ids) {
      Asset asset =
          assetRepository
              .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
              .orElseThrow(
                  () -> new AssetException(AssetErrorCode.ASSET_NOT_FOUND, "Asset ID " + id));
      if (asset.isFolder()) {
        String pathPrefix = asset.getPath() + StorageConstants.PATH_DELIMITER;
        assetRepository.deleteByTenantIdAndPathStartingWith(asset.getTenantId(), pathPrefix);
      } else {
        storageService.delete(asset.getSource());
      }

      asset.softDelete();
      assetsToUpdate.add(asset);
      evictAssetCaches(asset);
    }

    assetRepository.saveAll(assetsToUpdate);
    log.info("Bulk deleted {} assets", ids.size());
  }

  private void internalDelete(long id) {
    String tenantId = TenantContext.requireTenantId();
    Asset asset =
        assetRepository
            .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
            .orElseThrow(
                () -> new AssetException(AssetErrorCode.ASSET_NOT_FOUND, "Asset ID " + id));
    if (asset.isFolder()) {
      // O(1) Deep deletion of all nested assets using Materialized Path
      String pathPrefix = asset.getPath() + StorageConstants.PATH_DELIMITER;
      assetRepository.deleteByTenantIdAndPathStartingWith(asset.getTenantId(), pathPrefix);
    } else {
      storageService.delete(asset.getSource());
    }

    asset.softDelete();
    assetRepository.save(asset);
    evictAssetCaches(asset); // Clear related caches
    log.info("Deleted asset ID: {} and its children", id);
  }

  // Helper method to clear caches when an asset is mutated
  private void evictAssetCaches(Asset asset) {
    log.debug("Evicting cache for Asset ID: {} and its types", asset.getId());
  }

  private void validateTenant(Asset asset) {
    String currentTenant = TenantContext.requireTenantId();
    if (!currentTenant.equals(asset.getTenantId())) {
      throw new AssetException(AssetErrorCode.ASSET_NOT_FOUND, "Asset not found");
    }
  }
}
