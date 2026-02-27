package com.chamrong.iecommerce.asset.application.service;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.application.dto.UploadAssetMetadata;
import com.chamrong.iecommerce.asset.application.mapper.AssetMapper;
import com.chamrong.iecommerce.asset.application.metadata.CompositeMetadataExtractor;
import com.chamrong.iecommerce.asset.application.processing.ImageProcessingService;
import com.chamrong.iecommerce.asset.application.security.FileScanner;
import com.chamrong.iecommerce.asset.application.security.FileSecurityValidator;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.AssetException;
import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import com.chamrong.iecommerce.common.TenantContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetUploadService {

  private final AssetRepository assetRepository;
  private final com.chamrong.iecommerce.asset.domain.StorageService storageService;
  private final ImageProcessingService imageProcessingService;
  private final FileSecurityValidator fileSecurityValidator;
  private final FileScanner fileScanner;
  private final CompositeMetadataExtractor metadataExtractor;

  private static final long DEFAULT_QUOTA = 5L * 1024 * 1024 * 1024; // 5GB

  /**
   * Uploads an asset file to storage.
   *
   * @param file the multipart file to upload
   * @param metadata the additional metadata for the asset
   * @return the created asset response
   */
  @Transactional
  public @NonNull AssetResponse upload(
      @NonNull MultipartFile file, @NonNull UploadAssetMetadata metadata) {
    String tenantId = TenantContext.requireTenantId();
    checkQuota(tenantId, file.getSize());

    String mimeType =
        file.getContentType() != null ? file.getContentType() : StorageConstants.MIME_OCTET_STREAM;
    String originalName =
        file.getOriginalFilename() != null ? file.getOriginalFilename() : StorageConstants.UNKNOWN;

    try (InputStream inputStream = file.getInputStream()) {
      performSecurityChecks(originalName, inputStream);

      InputStream processedStream = processImageIfNecessary(file, inputStream, mimeType, metadata);
      long finalSize = getEffectiveSize(processedStream, file.getSize());

      String source = storageService.upload(originalName, mimeType, processedStream, finalSize);

      Map<String, Object> extractedMetadata = extractMetadata(file, mimeType);

      Asset asset = createAsset(tenantId, originalName, mimeType, finalSize, source, metadata);
      asset.setMetadata(extractedMetadata);

      log.info(
          "Asset uploaded to storage name={} size={} type={} path={}",
          originalName,
          finalSize,
          metadata.type(),
          metadata.path());

      return AssetMapper.toResponse(assetRepository.save(asset));
    } catch (IOException e) {
      log.error("Failed to read upload file stream", e);
      throw new StorageException(
          AssetErrorCode.STORAGE_OPERATION_FAILED, "Upload failed: " + e.getMessage());
    } catch (SecurityValidationException e) {
      throw e;
    } catch (AssetException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during asset upload", e);
      throw new AssetException(AssetErrorCode.INTERNAL_ERROR, e.getMessage());
    }
  }

  private void checkQuota(@NonNull String tenantId, long uploadSize) {
    long currentUsage = assetRepository.sumFileSizeByTenantIdAndDeletedAtIsNull(tenantId);
    if (currentUsage + uploadSize > DEFAULT_QUOTA) {
      throw new AssetException(
          AssetErrorCode.STORAGE_QUOTA_EXCEEDED,
          String.format(
              "Storage quota exceeded. Used: %d, Requested: %d, Limit: %d",
              currentUsage, uploadSize, DEFAULT_QUOTA));
    }
  }

  private void performSecurityChecks(@NonNull String name, @NonNull InputStream stream) {
    fileSecurityValidator.validate(name, stream);
    fileScanner.scan(stream, name);
  }

  private @NonNull InputStream processImageIfNecessary(
      @NonNull MultipartFile originalFile,
      @NonNull InputStream inputStream,
      @NonNull String mimeType,
      @NonNull UploadAssetMetadata metadata) {
    if (!mimeType.startsWith(StorageConstants.MIME_IMAGE_PREFIX)) {
      return inputStream;
    }
    return processImage(originalFile, inputStream, mimeType, metadata);
  }

  private @NonNull InputStream processImage(
      @NonNull MultipartFile originalFile,
      @NonNull InputStream inputStream,
      @NonNull String mimeType,
      @NonNull UploadAssetMetadata metadata) {
    try {
      InputStream currentStream = inputStream;
      String format = mimeType.replace(StorageConstants.MIME_IMAGE_PREFIX, "");

      if (metadata.width() != null && metadata.height() != null) {
        if (metadata.crop()) {
          currentStream =
              imageProcessingService.crop(
                  currentStream, metadata.width(), metadata.height(), format);
        } else {
          currentStream =
              imageProcessingService.resize(
                  currentStream, metadata.width(), metadata.height(), format);
        }
      }

      if (metadata.convertToWebP()) {
        currentStream = imageProcessingService.convertToWebP(currentStream);
      } else if (metadata.convertToAvif()) {
        currentStream = imageProcessingService.convertToAvif(currentStream);
      }

      return currentStream;
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

  private long getEffectiveSize(@NonNull InputStream stream, long originalSize) throws IOException {
    // If it's a ByteArrayInputStream (from processing), we can get exact size
    if (stream instanceof java.io.ByteArrayInputStream) {
      return stream.available();
    }
    return originalSize;
  }

  private @NonNull Map<String, Object> extractMetadata(
      @NonNull MultipartFile file, @NonNull String mimeType) {
    try (InputStream metadataStream = file.getInputStream()) {
      return metadataExtractor.extract(metadataStream, mimeType);
    } catch (IOException e) {
      log.warn("Failed to extract metadata, proceeding without it", e);
      return Collections.emptyMap();
    }
  }

  private @NonNull Asset createAsset(
      @NonNull String tenantId,
      @NonNull String name,
      @NonNull String mimeType,
      long size,
      @NonNull String source,
      @NonNull UploadAssetMetadata metadata) {
    Asset asset = new Asset();
    asset.setTenantId(tenantId);
    asset.setName(name);
    asset.setFileName(name);
    asset.setMimeType(mimeType);
    asset.setFileSize(size);
    asset.setSource(source);
    asset.setType(metadata.type());
    asset.setPath(metadata.path());
    asset.setFolder(false);
    asset.setPublic(metadata.isPublic());
    return asset;
  }
}
