package com.chamrong.iecommerce.asset.infrastructure.storage;

import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCloudStorageService implements StorageService {

  private final Storage storage;
  private final GoogleCloudStorageConfiguration config;

  @Override
  public String upload(String fileName, String contentType, InputStream inputStream, long size) {
    try {
      String key = UUID.randomUUID() + StorageConstants.KEY_SEPARATOR + fileName;
      BlobId blobId = BlobId.of(config.getBucketName(), key);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

      storage.createFrom(blobInfo, inputStream);
      log.info("Uploaded file to GCS: bucket={}, key={}", config.getBucketName(), key);
      return key;
    } catch (com.google.cloud.storage.StorageException | java.io.IOException e) {
      log.error("Failed to upload file to GCS", e);
      throw new StorageException(
          AssetErrorCode.STORAGE_OPERATION_FAILED, "Failed to upload file to Google Cloud Storage");
    }
  }

  @Override
  public String initiateMultipartUpload(String fileName, String contentType) {
    String key = UUID.randomUUID() + StorageConstants.KEY_SEPARATOR + fileName;
    String uploadId = UUID.randomUUID().toString();
    log.info("Initiated simulated multipart upload for GCS: key={}, uploadId={}", key, uploadId);
    return uploadId + "|" + key;
  }

  @Override
  public String uploadPart(
      String uploadId, String key, int partNumber, InputStream inputStream, long size) {
    try {
      String partKey = key + "_part_" + uploadId + "_" + partNumber;
      BlobId blobId = BlobId.of(config.getBucketName(), partKey);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

      storage.createFrom(blobInfo, inputStream);
      log.debug("Uploaded part {} for GCS key {}, partKey: {}", partNumber, key, partKey);

      return partKey; // Returning partKey as the token for complete.
    } catch (com.google.cloud.storage.StorageException | java.io.IOException e) {
      log.error("Failed to upload part {} for GCS key: {}", partNumber, key, e);
      throw new StorageException(
          AssetErrorCode.STORAGE_OPERATION_FAILED, "Part upload failed in GCS");
    }
  }

  @Override
  public String completeMultipartUpload(String uploadId, String key, Map<Integer, String> parts) {
    try {
      java.util.List<String> sortedPartKeys =
          parts.entrySet().stream()
              .sorted(Map.Entry.comparingByKey())
              .map(Map.Entry::getValue)
              .collect(Collectors.toList());

      if (sortedPartKeys.isEmpty()) {
        throw new StorageException(AssetErrorCode.VALIDATION_ERROR, "No parts to complete");
      }

      int batchSize = 32;
      java.util.List<String> currentLayer = sortedPartKeys;
      int iteration = 0;

      while (currentLayer.size() > 1) {
        java.util.List<String> nextLayer = new java.util.ArrayList<>();
        for (int i = 0; i < currentLayer.size(); i += batchSize) {
          int end = Math.min(i + batchSize, currentLayer.size());
          java.util.List<String> batch = currentLayer.subList(i, end);

          String targetKey = key + "_compose_" + uploadId + "_" + iteration + "_" + i;
          if (currentLayer.size() <= batchSize && iteration == 0) {
            targetKey = key;
          } else if (currentLayer.size() <= batchSize && iteration > 0) {
            targetKey = key;
          }

          BlobId targetBlobId = BlobId.of(config.getBucketName(), targetKey);
          BlobInfo targetBlobInfo = BlobInfo.newBuilder(targetBlobId).build();

          Storage.ComposeRequest.Builder composeBuilder =
              Storage.ComposeRequest.newBuilder().setTarget(targetBlobInfo);
          for (String pKey : batch) {
            composeBuilder.addSource(pKey);
          }

          storage.compose(composeBuilder.build());
          nextLayer.add(targetKey);
        }

        if (iteration > 0) {
          for (String tempKey : currentLayer) {
            storage.delete(BlobId.of(config.getBucketName(), tempKey));
          }
        }
        currentLayer = nextLayer;
        iteration++;
      }

      for (String pKey : sortedPartKeys) {
        storage.delete(BlobId.of(config.getBucketName(), pKey));
      }

      log.info("Completed simulated multipart upload for GCS key: {}", key);
      return key;
    } catch (com.google.cloud.storage.StorageException e) {
      log.error("Failed to complete multipart upload for GCS key: {}", key, e);
      throw new StorageException(
          AssetErrorCode.STORAGE_OPERATION_FAILED, "Completion failed in GCS");
    }
  }

  @Override
  public void abortMultipartUpload(String uploadId, String key) {
    try {
      Iterable<com.google.cloud.storage.Blob> blobs =
          storage
              .list(
                  config.getBucketName(), Storage.BlobListOption.prefix(key + "_part_" + uploadId))
              .iterateAll();
      for (com.google.cloud.storage.Blob blob : blobs) {
        blob.delete();
      }
      log.info("Aborted simulated multipart upload for GCS key: {}", key);
    } catch (com.google.cloud.storage.StorageException e) {
      log.warn("Failed to abort multipart upload for GCS key: {}", key, e);
    }
  }

  @Override
  public String getPublicUrl(String source) {
    return "https://storage.googleapis.com/"
        + config.getBucketName()
        + StorageConstants.PATH_DELIMITER
        + source;
  }

  @Override
  public java.util.Optional<String> generatePresignedUrl(String source) {
    try {
      BlobId blobId = BlobId.of(config.getBucketName(), source);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      // Pre-signed URL valid for 1 hour.
      URL signedUrl =
          storage.signUrl(blobInfo, 1, TimeUnit.HOURS, Storage.SignUrlOption.withV4Signature());
      return java.util.Optional.of(signedUrl.toString());
    } catch (com.google.cloud.storage.StorageException e) {
      log.error("Failed to generate pre-signed URL for GCS asset: {}", source, e);
      return java.util.Optional.empty();
    }
  }

  @Override
  public void delete(String source) {
    try {
      BlobId blobId = BlobId.of(config.getBucketName(), source);
      boolean deleted = storage.delete(blobId);
      if (deleted) {
        log.info("Deleted file from GCS: {}", source);
      } else {
        log.warn("File not found for deletion in GCS: {}", source);
      }
    } catch (com.google.cloud.storage.StorageException e) {
      log.error("Failed to delete file from GCS", e);
      throw new RuntimeException("Failed to delete file from Google Cloud Storage", e);
    }
  }

  @Override
  public String copy(String source, String destination) {
    try {
      BlobId sourceBlobId = BlobId.of(config.getBucketName(), source);
      BlobId targetBlobId = BlobId.of(config.getBucketName(), destination);

      storage.copy(
          Storage.CopyRequest.newBuilder().setSource(sourceBlobId).setTarget(targetBlobId).build());

      log.info("Copied file from {} to {} in GCS", source, destination);
      return destination;
    } catch (com.google.cloud.storage.StorageException e) {
      log.error("Failed to copy file in GCS from {} to {}", source, destination, e);
      throw new RuntimeException("Failed to copy file in Google Cloud Storage", e);
    }
  }

  @Override
  public String move(String source, String destination) {
    String newSource = copy(source, destination);
    delete(source);
    log.info("Moved file from {} to {} in GCS", source, destination);
    return newSource;
  }

  @Override
  public String createFolder(String folderPath) {
    if (!folderPath.endsWith(StorageConstants.PATH_DELIMITER)) {
      folderPath = folderPath + StorageConstants.PATH_DELIMITER;
    }
    try {
      BlobId blobId = BlobId.of(config.getBucketName(), folderPath);
      BlobInfo blobInfo =
          BlobInfo.newBuilder(blobId)
              .setContentType("application/x-www-form-urlencoded;charset=UTF-8")
              .build();

      storage.create(blobInfo, new byte[0]);
      log.info("Created virtual folder in GCS: {}", folderPath);
      return folderPath;
    } catch (com.google.cloud.storage.StorageException e) {
      log.error("Failed to create virtual folder in GCS: {}", folderPath, e);
      throw new RuntimeException("Failed to create folder in Google Cloud Storage", e);
    }
  }

  @Override
  public InputStream download(String source) {
    try {
      BlobId blobId = BlobId.of(config.getBucketName(), source);
      return java.nio.channels.Channels.newInputStream(storage.get(blobId).reader());
    } catch (com.google.cloud.storage.StorageException e) {
      log.error("Failed to download file from GCS: {}", source, e);
      throw new RuntimeException("Failed to download file from Google Cloud Storage", e);
    }
  }

  @Override
  public String getProviderName() {
    return StorageConstants.PROVIDER_GCS;
  }
}
