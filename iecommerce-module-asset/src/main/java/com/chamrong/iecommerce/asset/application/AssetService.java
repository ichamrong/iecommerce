package com.chamrong.iecommerce.asset.application;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.AssetType;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  // ── Commands ───────────────────────────────────────────────────────────────

  @Transactional
  public AssetResponse upload(String tenantId, MultipartFile file, AssetType type, String path) {
    String mimeType =
        file.getContentType() != null ? file.getContentType() : "application/octet-stream";
    String originalName =
        file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";

    try {
      String source =
          storageService.upload(originalName, mimeType, file.getInputStream(), file.getSize());

      Asset asset = new Asset();
      asset.setTenantId(tenantId);
      asset.setName(originalName);
      asset.setFileName(originalName);
      asset.setMimeType(mimeType);
      asset.setFileSize(file.getSize());
      asset.setSource(source);
      asset.setType(type);
      asset.setPath(path);

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
            .orElseThrow(() -> new EntityNotFoundException("Asset not found: " + id));

    storageService.delete(asset.getSource());
    assetRepository.deleteById(id);
    log.info("Asset deleted id={} source={}", id, asset.getSource());
  }

  // ── Queries ────────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public Optional<AssetResponse> findById(Long id) {
    return assetRepository.findById(id).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public List<AssetResponse> findByType(AssetType type) {
    return assetRepository.findByType(type).stream().map(this::toResponse).toList();
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private AssetResponse toResponse(Asset a) {
    return new AssetResponse(
        a.getId(),
        a.getName(),
        a.getFileName(),
        a.getMimeType(),
        a.getFileSize(),
        a.getSource(),
        a.getType().name(),
        a.getCreatedAt());
  }
}
