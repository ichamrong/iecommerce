package com.chamrong.iecommerce.asset.application.service;

import com.chamrong.iecommerce.asset.application.dto.AssetResponse;
import com.chamrong.iecommerce.asset.application.dto.CompleteMultipartUploadRequest;
import com.chamrong.iecommerce.asset.application.dto.InitiateMultipartUploadRequest;
import com.chamrong.iecommerce.asset.application.dto.InitiateMultipartUploadResponse;
import com.chamrong.iecommerce.asset.application.dto.UploadAssetMetadata;
import com.chamrong.iecommerce.asset.application.mapper.AssetMapper;
import com.chamrong.iecommerce.asset.domain.Asset;
import com.chamrong.iecommerce.asset.domain.AssetRepository;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.common.TenantContext;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetChunkedUploadService {

  private final AssetRepository assetRepository;
  private final StorageService storageService;

  public InitiateMultipartUploadResponse initiateMultipartUpload(
      InitiateMultipartUploadRequest request) {
    // Optionally check quota here, assuming total size is not known yet or passed in metadata.
    String result =
        storageService.initiateMultipartUpload(request.getFileName(), request.getContentType());

    // Result format depends on provider implementation. We assume "uploadId|key" format for some
    // providers,
    // or we just return the result as uploadId if they don't combine.
    // In our implementations (R2/GCS), we returned "uploadId|key".
    String[] parts = result.split("\\|");
    if (parts.length == 2) {
      return new InitiateMultipartUploadResponse(parts[0], parts[1]);
    } else {
      // Best effort fallback
      return new InitiateMultipartUploadResponse(result, request.getFileName());
    }
  }

  public String uploadPart(
      String uploadId, String key, int partNumber, InputStream inputStream, long size) {
    return storageService.uploadPart(uploadId, key, partNumber, inputStream, size);
  }

  @Transactional
  public AssetResponse completeMultipartUpload(CompleteMultipartUploadRequest request) {
    String tenantId = TenantContext.requireTenantId();

    // 1. Complete at storage provider
    String fullKey =
        storageService.completeMultipartUpload(
            request.getUploadId(), request.getKey(), request.getParts());
    UploadAssetMetadata metadata = request.getMetadata();

    // 2. We don't have the final exact size synchronously if assembled by S3/GCS.
    // Usually, you should issue an SDK HeadObject call to get the exact size, but to keep the
    // interface simple,
    // we can set size to 0 or fetch it from storage provider logic. For simplicity, we assume 0 or
    // handle it separately.
    long finalSize = 0L; // TODO: Implement get size from metadata or HeadObject

    String mimeType =
        "application/octet-stream"; // A good default, ideally fetch from metadata/request

    // 3. Save to database
    Asset asset =
        Asset.create(
            tenantId,
            metadata.path() != null ? metadata.path() : request.getKey(),
            request.getKey(),
            mimeType,
            finalSize,
            fullKey,
            metadata.type(),
            metadata.path(),
            metadata.isPublic());

    log.info("Completed multipart upload for asset: key={}", request.getKey());
    return AssetMapper.toResponse(assetRepository.save(asset));
  }

  public void abortMultipartUpload(String uploadId, String key) {
    storageService.abortMultipartUpload(uploadId, key);
  }
}
