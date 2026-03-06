package com.chamrong.iecommerce.asset.application.service;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.application.dto.AssetStreamResponse;
import com.chamrong.iecommerce.asset.application.mapper.AssetMapper;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.AssetType;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.AssetException;
import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import com.chamrong.iecommerce.common.EventDispatcher;
import com.chamrong.iecommerce.common.TenantContext;
import com.chamrong.iecommerce.common.event.AssetAccessedEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetRetrievalService {

  private final AssetRepository assetRepository;
  private final StorageService storageService;
  private final EventDispatcher eventDispatcher;

  private static final int BUFFER_SIZE = 64 * 1024; // 64KB buffer for pipe

  /**
   * Retrieves a resource for proxy download, checking permissions.
   *
   * @param id the asset ID
   * @param isAuthorized whether the user has 'assets:read' authority
   * @return the asset metadata and its data stream
   */
  public AssetStreamResponse getProxyResource(long id, boolean isAuthorized) {
    Asset asset = findAssetById(id);

    // Security Check: If private, require authorized access
    if (!asset.isPublic() && !isAuthorized) {
      log.warn("Unauthorized proxy download attempt for Asset ID: {}", id);
      throw new SecurityValidationException(
          AssetErrorCode.UNAUTHORIZED_ACCESS, "Unauthorized access to private asset");
    }

    validateTenant(asset);
    InputStream inputStream = storageService.download(asset.getSource());
    return new AssetStreamResponse(AssetMapper.toResponse(asset), inputStream);
  }

  /**
   * Finds matching assets by name query.
   *
   * @param query the search query
   * @return list of asset responses
   */
  @Transactional(readOnly = true)
  public List<AssetResponse> searchByName(String query) {
    String tenantId = TenantContext.requireTenantId();
    return assetRepository
        .findByTenantIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(tenantId, query)
        .stream()
        .map(AssetMapper::toResponse)
        .toList();
  }

  /**
   * Searches for assets within a size range.
   *
   * @param minSize minimum size
   * @param maxSize maximum size
   * @return list of asset responses
   */
  @Transactional(readOnly = true)
  public List<AssetResponse> searchBySize(long minSize, long maxSize) {
    String tenantId = TenantContext.requireTenantId();
    return assetRepository
        .findByTenantIdAndFileSizeBetweenAndDeletedAtIsNull(tenantId, minSize, maxSize)
        .stream()
        .map(AssetMapper::toResponse)
        .toList();
  }

  /**
   * Finds an asset by ID.
   *
   * @param id the asset ID
   * @return optional asset response
   */
  @Transactional(readOnly = true)
  @org.springframework.cache.annotation.Cacheable(value = "assets", key = "#id")
  public Optional<AssetResponse> findById(long id) {
    String tenantId = TenantContext.requireTenantId();
    return assetRepository
        .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
        .map(AssetMapper::toResponse);
  }

  /**
   * Finds assets by type.
   *
   * @param type the asset type
   * @return list of asset responses
   */
  @Transactional(readOnly = true)
  public List<AssetResponse> findByType(AssetType type) {
    String tenantId = TenantContext.requireTenantId();
    return assetRepository.findByTenantIdAndTypeAndDeletedAtIsNull(tenantId, type).stream()
        .map(AssetMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public String getDownloadUrl(long id, String requestedBy, String ipAddress) {
    String tenantId = TenantContext.requireTenantId();
    Asset asset =
        assetRepository
            .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
            .orElseThrow(
                () -> new AssetException(AssetErrorCode.ASSET_NOT_FOUND, "Asset not found: " + id));

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

    return storageService
        .generatePresignedUrl(asset.getSource())
        .orElseGet(() -> storageService.getPublicUrl(asset.getSource()));
  }

  public InputStream download(long id) {
    Asset asset = findAssetById(id);
    return storageService.download(asset.getSource());
  }

  /**
   * Generates a ZIP stream for bulk download of assets.
   *
   * @param ids list of asset IDs to include in the ZIP
   * @return input stream for the streamed ZIP content
   */
  public InputStream bulkDownload(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return new java.io.ByteArrayInputStream(new byte[0]);
    }

    try {
      PipedInputStream pin = new PipedInputStream(BUFFER_SIZE);
      PipedOutputStream pout = new PipedOutputStream(pin);

      String tenantId = TenantContext.getCurrentTenant();
      // We use a separate thread to avoid deadlocks with PipedInputStream/PipedOutputStream
      CompletableFuture.runAsync(() -> streamAssetsToZip(ids, tenantId, pout));

      return pin;
    } catch (IOException e) {
      log.error("Failed to initialize bulk download pipes", e);
      throw new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, "Bulk download failed");
    }
  }

  private void streamAssetsToZip(List<Long> ids, String tenantId, PipedOutputStream pout) {
    if (tenantId != null) {
      TenantContext.setCurrentTenant(tenantId);
    }
    try (pout;
        ZipOutputStream zos = new ZipOutputStream(pout)) {
      for (Long id : ids) {
        assetRepository
            .findByIdAndDeletedAtIsNull(id)
            .ifPresent(asset -> addAssetToZip(asset, zos));
      }
    } catch (IOException e) {
      log.error("Critical error in bulk download ZIP streaming", e);
    } finally {
      TenantContext.clear();
    }
  }

  private void addAssetToZip(Asset asset, ZipOutputStream zos) {
    validateTenant(asset);
    if (asset.isFolder()) {
      return;
    }
    try (InputStream is = storageService.download(asset.getSource())) {
      ZipEntry entry = new ZipEntry(asset.getFileName());
      zos.putNextEntry(entry);
      is.transferTo(zos);
      zos.closeEntry();
    } catch (IOException e) {
      log.error("Failed to add asset {} to ZIP", asset.getId(), e);
    }
  }

  private Asset findAssetById(long id) {
    String tenantId = TenantContext.requireTenantId();
    return assetRepository
        .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
        .orElseThrow(() -> new AssetException(AssetErrorCode.ASSET_NOT_FOUND));
  }

  private void validateTenant(Asset asset) {
    String currentTenant = TenantContext.requireTenantId();
    if (!currentTenant.equals(asset.getTenantId())) {
      throw new AssetException(AssetErrorCode.ASSET_NOT_FOUND, "Asset not found");
    }
  }
}
