package com.chamrong.iecommerce.asset.infrastructure.storage;

import com.chamrong.iecommerce.asset.domain.StorageService;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2StorageService implements StorageService {

  private final S3Client s3Client;
  private final R2StorageConfiguration config;

  @Override
  public String upload(String fileName, String contentType, InputStream inputStream, long size) {
    String key = UUID.randomUUID() + "-" + fileName;

    try {
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(config.getBucketName())
              .key(key)
              .contentType(contentType)
              .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));
      log.info("Uploaded asset to R2: bucket={}, key={}", config.getBucketName(), key);
      return key;
    } catch (Exception e) {
      log.error("Failed to upload asset to R2", e);
      throw new RuntimeException("Storage upload failed", e);
    }
  }

  @Override
  public String getPublicUrl(String source) {
    if (config.getPublicUrl() == null || config.getPublicUrl().isEmpty()) {
      return source;
    }
    return config.getPublicUrl().endsWith("/")
        ? config.getPublicUrl() + source
        : config.getPublicUrl() + "/" + source;
  }

  @Override
  public void delete(String source) {
    try {
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(config.getBucketName()).key(source).build();
      s3Client.deleteObject(deleteObjectRequest);
      log.info("Deleted asset from R2: key={}", source);
    } catch (Exception e) {
      log.warn("Failed to delete asset from R2: key={}", source, e);
    }
  }
}
