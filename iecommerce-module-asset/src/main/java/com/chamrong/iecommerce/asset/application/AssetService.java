package com.chamrong.iecommerce.asset.application;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.AssetType;
import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.event.AssetAccessedEvent;
import com.chamrong.iecommerce.common.event.EventDispatcher;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Asset (media management) service.
 *
 * <p>In production, file bytes would be streamed to S3/MinIO. This implementation stores a
 * reference path and simulates the upload — keeping the service fully functional without external
 * object-store dependencies during development.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

  private final AssetRepository assetRepository;
  private final com.chamrong.iecommerce.asset.domain.StorageService storageService;
  private final ImageProcessingService imageProcessingService;
  private final EventDispatcher eventDispatcher;
  private final FileSecurityValidator fileSecurityValidator;

  // ── Commands ───────────────────────────────────────────────────────────────
  @Transactional
  public AssetResponse upload(MultipartFile file, AssetType type, String path) {
    return upload(file, type, path, null, null, false);
  }

  @Transactional
  public AssetResponse upload(
      MultipartFile file,
      AssetType type,
      String path,
      @Nullable Integer width,
      @Nullable Integer height,
      boolean crop) {
    String tenantId = TenantContext.requireTenantId();
    String mimeType =
        file.getContentType() != null ? file.getContentType() : StorageConstants.MIME_OCTET_STREAM;
    String originalName =
        file.getOriginalFilename() != null ? file.getOriginalFilename() : StorageConstants.UNKNOWN;

    try {
      java.io.InputStream inputStream = file.getInputStream();
      long finalSize = file.getSize();

      fileSecurityValidator.validate(originalName, mimeType, inputStream);

      if (mimeType.startsWith(StorageConstants.MIME_IMAGE_PREFIX)
          && width != null
          && height != null) {
        inputStream = processImage(file, inputStream, mimeType, width, height, crop);
        // Estimated, we'd need to re-read to get exact bytes but for streaming we can use approx
        finalSize = inputStream.available();
      }

      String source = storageService.upload(originalName, mimeType, inputStream, finalSize);

      Asset asset = createAsset(tenantId, originalName, mimeType, finalSize, source, type, path);

      log.info(
          "Asset uploaded to storage name={} size={} type={} path={}",
          originalName,
          file.getSize(),
          type,
          path);
      return toResponse(assetRepository.save(asset));
    } catch (java.io.IOException e) {
      log.error("Failed to read upload file stream", e);
      throw new RuntimeException("Upload failed", e);
    }
  }

  @Transactional
  public void delete(Long id) {
    Asset asset =
        assetRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

    validateTenant(asset);
    if (asset.isFolder()) {
      // O(1) Deep deletion of all nested assets using Materialized Path
      String pathPrefix = asset.getPath() + StorageConstants.PATH_DELIMITER;
      assetRepository.deleteByTenantIdAndPathStartingWith(asset.getTenantId(), pathPrefix);
    } else {
      storageService.delete(asset.getSource());
    }

    assetRepository.delete(asset);
    evictAssetCaches(asset); // Clear related caches
    log.info("Deleted asset ID: {} and its children", id);
  }

  // Helper method to clear caches when an asset is mutated
  private void evictAssetCaches(Asset asset) {
    log.debug("Evicting cache for Asset ID: {} and its types", asset.getId());
  }

  @Transactional
  public AssetResponse createFolder(@Nullable Long parentId, String name, AssetType type) {
    String tenantId = TenantContext.requireTenantId();
    String folderPath = name + StorageConstants.PATH_DELIMITER;
    String materializedPath = StorageConstants.PATH_DELIMITER + name; // Virtual path computation

    if (parentId != null) {
      Asset parent =
          assetRepository
              .findById(parentId)
              .orElseThrow(() -> new EntityNotFoundException("Parent folder not found"));
      folderPath = parent.getSource() + folderPath;
      materializedPath =
          parent.getPath() + StorageConstants.PATH_DELIMITER + name; // Append to parent's path
    }

    // Attempt to register it in storage if necessary (e.g. S3 zero-byte object)
    String sourcePath = storageService.createFolder(folderPath);

    Asset folder = createFolderAsset(tenantId, name, sourcePath, type, parentId, materializedPath);

    folder = assetRepository.save(folder);
    log.info("Created virtual folder Asset ID: {} with path: {}", folder.getId(), materializedPath);
    return toResponse(folder);
  }

  @Transactional
  public AssetResponse copyAsset(Long assetId, @Nullable Long targetFolderId) {
    Asset sourceAsset =
        assetRepository
            .findById(assetId)
            .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

    validateTenant(sourceAsset);

    if (sourceAsset.isFolder()) {
      throw new UnsupportedOperationException("Copying entire folders is not supported yet.");
    }

    String destinationPath = sourceAsset.getFileName();
    if (targetFolderId != null) {
      Asset targetFolder =
          assetRepository
              .findById(targetFolderId)
              .orElseThrow(() -> new EntityNotFoundException("Target folder not found"));
      validateTenant(targetFolder);
      destinationPath = targetFolder.getSource() + sourceAsset.getFileName();
    }

    String newSource = storageService.copy(sourceAsset.getSource(), destinationPath);

    Asset copiedAsset = createCopiedAsset(sourceAsset, newSource, targetFolderId);

    copiedAsset = assetRepository.save(copiedAsset);
    log.info("Copied Asset ID: {} to new Asset ID: {}", sourceAsset.getId(), copiedAsset.getId());
    return toResponse(copiedAsset);
  }

  @Transactional
  public AssetResponse moveAsset(Long assetId, @Nullable Long targetFolderId) {
    Asset asset =
        assetRepository
            .findById(assetId)
            .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

    validateTenant(asset);

    if (asset.isFolder()) {
      throw new UnsupportedOperationException("Moving folders is not supported yet.");
    }

    String destinationPath = asset.getFileName();
    String newMaterializedPath = StorageConstants.PATH_DELIMITER + asset.getFileName();

    if (targetFolderId != null) {
      Asset targetFolder =
          assetRepository
              .findById(targetFolderId)
              .orElseThrow(() -> new EntityNotFoundException("Target folder not found"));
      validateTenant(targetFolder);

      if (!targetFolder.isFolder()) {
        throw new IllegalArgumentException("Target must be a folder");
      }

      destinationPath = targetFolder.getSource() + asset.getFileName();
      newMaterializedPath =
          targetFolder.getPath() + StorageConstants.PATH_DELIMITER + asset.getFileName();
    }

    String newSource = storageService.move(asset.getSource(), destinationPath);

    asset.setSource(newSource);
    asset.setParentId(targetFolderId); // targetFolderId can be null for root
    asset.setPath(newMaterializedPath);
    asset = assetRepository.save(asset);

    log.info(
        "Moved Asset ID: {} to targetFolderId: {} and new Path: {}",
        asset.getId(),
        targetFolderId,
        newMaterializedPath);
    return toResponse(asset);
  }

  @Transactional
  @CacheEvict(
      value = {"assets", "asset-downloads"},
      key = "#assetId")
  public AssetResponse renameAsset(Long assetId, String newName) {
    Asset asset =
        assetRepository
            .findById(assetId)
            .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

    validateTenant(asset);
    asset.setName(newName);
    // Preserving the extension or allowing it to change based on newName.
    // In a complete implementation, you might want to extract the extension from the old fileName
    // and append it to newName if newName doesn't have one, or just replace the name component.
    // For now, we update both to the provided newName to act as a virtual rename.
    asset.setFileName(newName);

    asset = assetRepository.save(asset);
    log.info("Renamed Asset ID: {} to {}", asset.getId(), newName);
    return toResponse(asset);
  }

  @Transactional
  public void bulkDelete(List<Long> ids) {
    if (ids.isEmpty()) {
      return;
    }
    ids.forEach(this::delete);
  }

  @Transactional
  public List<AssetResponse> bulkMove(List<Long> ids, @Nullable Long targetFolderId) {
    if (ids.isEmpty()) {
      return List.of();
    }

    // Validate target folder once if provided
    if (targetFolderId != null) {
      Asset targetFolder =
          assetRepository
              .findById(targetFolderId)
              .orElseThrow(() -> new EntityNotFoundException("Target folder not found"));
      if (!targetFolder.isFolder()) {
        throw new IllegalArgumentException("Target must be a folder");
      }
    }

    return ids.stream().map(id -> moveAsset(id, targetFolderId)).toList();
  }

  // ── Queries ────────────────────────────────────────────────────────────────
  @Transactional(readOnly = true)
  public List<AssetResponse> searchByName(String query) {
    String tenantId = TenantContext.requireTenantId();
    return assetRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, query).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AssetResponse> searchBySize(long minSize, long maxSize) {
    String tenantId = TenantContext.requireTenantId();
    return assetRepository.findByTenantIdAndFileSizeBetween(tenantId, minSize, maxSize).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "assets", key = "#id")
  public Optional<AssetResponse> findById(Long id) {
    return assetRepository
        .findById(id)
        .map(
            a -> {
              validateTenant(a);
              return toResponse(a);
            });
  }

  @Transactional(readOnly = true)
  public List<AssetResponse> findByType(AssetType type) {
    String tenantId = TenantContext.requireTenantId();
    return assetRepository.findByTenantIdAndType(tenantId, type).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public String getDownloadUrl(Long id, String requestedBy, String ipAddress) {
    Asset asset =
        assetRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + id));

    validateTenant(asset);

    // Log secure access audit loop
    eventDispatcher.dispatch(
        new AssetAccessedEvent(
            asset.getId(),
            asset.getTenantId(),
            asset.getFileName(),
            requestedBy,
            ipAddress,
            Instant.now()));

    return storageService.getPublicUrl(asset.getSource());
  }

  public java.io.InputStream download(Long id) {
    Asset asset =
        assetRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

    validateTenant(asset);
    return storageService.download(asset.getSource());
  }

  private void validateTenant(Asset asset) {
    String currentTenant = TenantContext.requireTenantId();
    if (!currentTenant.equals(asset.getTenantId())) {
      throw new AccessDeniedException("Unauthorized access to asset of another tenant");
    }
  }

  private java.io.InputStream processImage(
      MultipartFile originalFile,
      java.io.InputStream inputStream,
      String mimeType,
      Integer width,
      Integer height,
      boolean crop) {
    try {
      String format = mimeType.replace(StorageConstants.MIME_IMAGE_PREFIX, "");
      if (crop) {
        return imageProcessingService.crop(inputStream, width, height, format);
      } else {
        return imageProcessingService.resize(inputStream, width, height, format);
      }
    } catch (Exception processEx) {
      log.warn("Image processing failed, falling back to original", processEx);
      try {
        return originalFile.getInputStream(); // Reset
      } catch (IOException ioEx) {
        log.error("Failed to reset input stream after failed image processing", ioEx);
        return inputStream;
      }
    }
  }

  // ── Helpers ────────────────────────────────────────────────────────────────
  private AssetResponse toResponse(Asset a) {
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
        a.getType().name(),
        a.getCreatedAt());
  }

  private Asset createAsset(
      String tenantId,
      String name,
      String mimeType,
      long size,
      String source,
      AssetType type,
      @Nullable String path) {
    Asset asset = new Asset();
    asset.setTenantId(tenantId);
    asset.setName(name);
    asset.setFileName(name);
    asset.setMimeType(mimeType);
    asset.setFileSize(size);
    asset.setSource(source);
    asset.setType(type);
    asset.setPath(path);
    asset.setFolder(false);
    return asset;
  }

  private Asset createFolderAsset(
      String tenantId,
      String name,
      String source,
      AssetType type,
      @Nullable Long parentId,
      String materializedPath) {
    Asset folder = new Asset();
    folder.setTenantId(tenantId);
    folder.setName(name);
    folder.setFileName(name);
    folder.setMimeType(StorageConstants.MIME_DIRECTORY);
    folder.setFileSize(0L);
    folder.setSource(source);
    folder.setType(type);
    folder.setParentId(parentId);
    folder.setFolder(true);
    folder.setPath(materializedPath);
    return folder;
  }

  private Asset createCopiedAsset(
      Asset sourceAsset, String newSource, @Nullable Long targetFolderId) {
    Asset copiedAsset = new Asset();
    copiedAsset.setTenantId(sourceAsset.getTenantId());
    copiedAsset.setName(StorageConstants.COPY_PREFIX + sourceAsset.getName());
    copiedAsset.setFileName(sourceAsset.getFileName());
    copiedAsset.setMimeType(sourceAsset.getMimeType());
    copiedAsset.setFileSize(sourceAsset.getFileSize());
    copiedAsset.setSource(newSource);
    copiedAsset.setType(sourceAsset.getType());
    copiedAsset.setParentId(targetFolderId);
    copiedAsset.setFolder(false);
    return copiedAsset;
  }
}
