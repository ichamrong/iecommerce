package com.chamrong.iecommerce.asset.infrastructure.storage;

import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
      throw new RuntimeException("Failed to upload file to Google Cloud Storage", e);
    }
  }

  @Override
  public String getPublicUrl(String source) {
    try {
      BlobId blobId = BlobId.of(config.getBucketName(), source);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      // Pre-signed URL valid for 1 hour.
      URL signedUrl =
          storage.signUrl(blobInfo, 1, TimeUnit.HOURS, Storage.SignUrlOption.withV4Signature());
      return signedUrl.toString();
    } catch (com.google.cloud.storage.StorageException e) {
      log.error("Failed to generate pre-signed URL for GCS asset: {}", source, e);
      throw new RuntimeException("Failed to generate GCS pre-signed URL", e);
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
