package com.chamrong.iecommerce.asset.application.service;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.application.mapper.AssetMapper;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.AssetType;
import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.AssetException;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetTransferService {

  private final AssetRepository assetRepository;
  private final StorageService storageService;

  private static final String MSG_FOLDER_NOT_FOUND = "Target folder not found";
  private static final String MSG_MUST_BE_FOLDER = "Target must be a folder";

  /**
   * Creates a virtual folder asset.
   *
   * @param parentId optional parent folder ID
   * @param name folder name
   * @param type asset type (should be FOLDER)
   * @return the created folder response
   */
  @Transactional
  public AssetResponse createFolder(@Nullable Long parentId, String name, AssetType type) {
    String tenantId = TenantContext.requireTenantId();
    String folderPath = name + StorageConstants.PATH_DELIMITER;
    String materializedPath = StorageConstants.PATH_DELIMITER + name; // Virtual path computation

    if (parentId != null) {
      Asset parent =
          assetRepository
              .findByIdAndDeletedAtIsNull(parentId)
              .orElseThrow(
                  () ->
                      new AssetException(
                          AssetErrorCode.ASSET_NOT_FOUND, "Parent folder not found"));
      folderPath = parent.getSource() + folderPath;
      materializedPath =
          parent.getPath() + StorageConstants.PATH_DELIMITER + name; // Append to parent's path
    }

    // Attempt to register it in storage if necessary (e.g. S3 zero-byte object)
    String sourcePath = storageService.createFolder(folderPath);

    Asset folder = createFolderAsset(tenantId, name, sourcePath, type, parentId, materializedPath);

    folder = assetRepository.save(folder);
    log.info("Created virtual folder Asset ID: {} with path: {}", folder.getId(), materializedPath);
    return AssetMapper.toResponse(folder);
  }

  /**
   * Copies an asset to a target folder.
   *
   * @param assetId source asset ID
   * @param targetFolderId optional target folder ID
   * @return the new asset response
   */
  @Transactional
  public AssetResponse copyAsset(long assetId, @Nullable Long targetFolderId) {
    Asset sourceAsset = findAssetById(assetId);
    validateTenant(sourceAsset);

    if (sourceAsset.isFolder()) {
      throw new UnsupportedOperationException("Copying entire folders is not supported yet.");
    }

    Asset targetFolder = resolveTargetFolder(targetFolderId);
    String destinationPath = computeDestinationPath(sourceAsset, targetFolder);

    String newSource = storageService.copy(sourceAsset.getSource(), destinationPath);
    Asset copiedAsset = createCopiedAsset(sourceAsset, newSource, targetFolderId);

    copiedAsset = assetRepository.save(copiedAsset);
    log.info("Copied Asset ID: {} to new Asset ID: {}", sourceAsset.getId(), copiedAsset.getId());
    return AssetMapper.toResponse(copiedAsset);
  }

  /**
   * Moves an asset to a target folder.
   *
   * @param assetId source asset ID
   * @param targetFolderId optional target folder ID
   * @return the updated asset response
   */
  @Transactional
  public AssetResponse moveAsset(long assetId, @Nullable Long targetFolderId) {
    return AssetMapper.toResponse(internalMove(assetId, targetFolderId));
  }

  /**
   * Bulk moves assets to a target folder.
   *
   * @param ids list of asset IDs to move
   * @param targetFolderId optional target folder ID
   * @return list of updated asset responses
   */
  @Transactional
  public List<AssetResponse> bulkMove(List<Long> ids, @Nullable Long targetFolderId) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }

    Asset targetFolder = resolveTargetFolder(targetFolderId);
    List<Asset> movedAssets = new java.util.ArrayList<>();

    for (Long id : ids) {
      Asset asset = findAssetById(id);
      validateTenant(asset);

      if (asset.isFolder()) {
        throw new UnsupportedOperationException("Moving folders is not supported yet.");
      }

      String destinationPath = computeDestinationPath(asset, targetFolder);
      String newMaterializedPath = computeMaterializedPath(asset, targetFolder);

      String newSource = storageService.move(asset.getSource(), destinationPath);
      asset.moveTo(newSource, targetFolderId, newMaterializedPath);
      movedAssets.add(asset);

      log.info(
          "Moved Asset ID: {} to targetFolderId: {} and new Path: {}",
          asset.getId(),
          targetFolderId,
          newMaterializedPath);
    }

    return assetRepository.saveAll(movedAssets).stream().map(AssetMapper::toResponse).toList();
  }

  @Transactional
  @CacheEvict(
      value = {"assets", "asset-downloads"},
      key = "#assetId")
  public AssetResponse renameAsset(long assetId, String newName) {
    return AssetMapper.toResponse(internalRename(assetId, newName));
  }

  /**
   * Bulk renames assets.
   *
   * @param idToNewNameMap map of asset ID to new name
   * @return list of updated asset responses
   */
  @Transactional
  public List<AssetResponse> bulkRename(Map<Long, String> idToNewNameMap) {
    if (idToNewNameMap == null || idToNewNameMap.isEmpty()) {
      return List.of();
    }

    List<Asset> renamedAssets = new java.util.ArrayList<>();

    for (Map.Entry<Long, String> entry : idToNewNameMap.entrySet()) {
      Long assetId = entry.getKey();
      String newName = entry.getValue();

      Asset asset = findAssetById(assetId);
      validateTenant(asset);
      String oldFileName = asset.getFileName();
      String newFileName;
      if (oldFileName != null && oldFileName.contains(".") && !newName.contains(".")) {
        String extension = oldFileName.substring(oldFileName.lastIndexOf("."));
        newFileName = newName + extension;
      } else {
        newFileName = newName;
      }
      asset.rename(newName, newFileName);
      renamedAssets.add(asset);
      log.info("Renamed Asset ID: {} to {}", asset.getId(), newName);
    }

    return assetRepository.saveAll(renamedAssets).stream().map(AssetMapper::toResponse).toList();
  }

  private Asset internalMove(long assetId, @Nullable Long targetFolderId) {
    Asset asset = findAssetById(assetId);
    validateTenant(asset);

    if (asset.isFolder()) {
      throw new UnsupportedOperationException("Moving folders is not supported yet.");
    }

    Asset targetFolder = resolveTargetFolder(targetFolderId);
    String destinationPath = computeDestinationPath(asset, targetFolder);
    String newMaterializedPath = computeMaterializedPath(asset, targetFolder);

    String newSource = storageService.move(asset.getSource(), destinationPath);
    asset.moveTo(newSource, targetFolderId, newMaterializedPath);
    asset = assetRepository.save(asset);

    log.info(
        "Moved Asset ID: {} to targetFolderId: {} and new Path: {}",
        asset.getId(),
        targetFolderId,
        newMaterializedPath);
    return asset;
  }

  private Asset internalRename(long assetId, String newName) {
    Asset asset = findAssetById(assetId);
    validateTenant(asset);
    String oldFileName = asset.getFileName();
    String newFileName;
    if (oldFileName != null && oldFileName.contains(".") && !newName.contains(".")) {
      String extension = oldFileName.substring(oldFileName.lastIndexOf("."));
      newFileName = newName + extension;
    } else {
      newFileName = newName;
    }
    asset.rename(newName, newFileName);

    asset = assetRepository.save(asset);
    log.info("Renamed Asset ID: {} to {}", asset.getId(), newName);
    return asset;
  }

  private Asset findAssetById(long id) {
    return assetRepository
        .findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new AssetException(AssetErrorCode.ASSET_NOT_FOUND));
  }

  private @Nullable Asset resolveTargetFolder(@Nullable Long targetFolderId) {
    if (targetFolderId == null) {
      return null;
    }
    Asset targetFolder =
        assetRepository
            .findByIdAndDeletedAtIsNull(targetFolderId)
            .orElseThrow(
                () -> new AssetException(AssetErrorCode.ASSET_NOT_FOUND, MSG_FOLDER_NOT_FOUND));
    validateTenant(targetFolder);
    if (!targetFolder.isFolder()) {
      throw new IllegalArgumentException(MSG_MUST_BE_FOLDER);
    }
    return targetFolder;
  }

  private String computeDestinationPath(Asset asset, @Nullable Asset targetFolder) {
    if (targetFolder == null) {
      return asset.getFileName();
    }
    return targetFolder.getSource() + asset.getFileName();
  }

  private String computeMaterializedPath(Asset asset, @Nullable Asset targetFolder) {
    if (targetFolder == null) {
      return StorageConstants.PATH_DELIMITER + asset.getFileName();
    }
    return targetFolder.getPath() + StorageConstants.PATH_DELIMITER + asset.getFileName();
  }

  private void validateTenant(Asset asset) {
    String currentTenant = TenantContext.requireTenantId();
    if (!currentTenant.equals(asset.getTenantId())) {
      throw new AccessDeniedException("Unauthorized access to asset of another tenant");
    }
  }

  private Asset createFolderAsset(
      String tenantId,
      String name,
      String source,
      AssetType type,
      @Nullable Long parentId,
      String materializedPath) {
    return Asset.folder(
        tenantId, name, source, type, parentId, materializedPath, StorageConstants.MIME_DIRECTORY);
  }

  private Asset createCopiedAsset(
      Asset sourceAsset, String newSource, @Nullable Long targetFolderId) {
    return Asset.copyOf(
        sourceAsset,
        newSource,
        StorageConstants.COPY_PREFIX + sourceAsset.getName(),
        targetFolderId);
  }
}
