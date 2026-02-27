package com.chamrong.iecommerce.asset.infrastructure.storage;

import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2StorageService implements StorageService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final R2StorageConfiguration config;

  @Override
  public String upload(String fileName, String contentType, InputStream inputStream, long size) {
    String key = UUID.randomUUID() + StorageConstants.KEY_SEPARATOR + fileName;

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
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.error("Failed to upload asset to R2", e);
      throw new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, "Storage upload failed");
    }
  }

  @Override
  public String initiateMultipartUpload(String fileName, String contentType) {
    String key = UUID.randomUUID() + StorageConstants.KEY_SEPARATOR + fileName;
    try {
      CreateMultipartUploadRequest createRequest =
          CreateMultipartUploadRequest.builder()
              .bucket(config.getBucketName())
              .key(key)
              .contentType(contentType)
              .build();

      CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createRequest);
      log.info("Initiated multipart upload for key: {}, uploadId: {}", key, response.uploadId());
      // Return a combined string so the caller has both pieces, or just return uploadId if the
      // caller manages the key.
      // Usually the caller needs the generated key. We will encode it as "uploadId|key".
      return response.uploadId() + "|" + key;
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.error("Failed to initiate multipart upload to R2", e);
      throw new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, "Multipart init failed");
    }
  }

  @Override
  public String uploadPart(
      String uploadId, String key, int partNumber, InputStream inputStream, long size) {
    try {
      UploadPartRequest uploadPartRequest =
          UploadPartRequest.builder()
              .bucket(config.getBucketName())
              .key(key)
              .uploadId(uploadId)
              .partNumber(partNumber)
              .build();

      UploadPartResponse response =
          s3Client.uploadPart(uploadPartRequest, RequestBody.fromInputStream(inputStream, size));
      log.debug("Uploaded part {} for key: {}, ETag: {}", partNumber, key, response.eTag());
      return response.eTag();
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.error("Failed to upload part {} for key: {}", partNumber, key, e);
      throw new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, "Part upload failed");
    }
  }

  @Override
  public String completeMultipartUpload(String uploadId, String key, Map<Integer, String> parts) {
    try {
      java.util.List<CompletedPart> completedParts =
          parts.entrySet().stream()
              .map(e -> CompletedPart.builder().partNumber(e.getKey()).eTag(e.getValue()).build())
              .collect(Collectors.toList());

      CompletedMultipartUpload completedMultipartUpload =
          CompletedMultipartUpload.builder().parts(completedParts).build();

      CompleteMultipartUploadRequest completeRequest =
          CompleteMultipartUploadRequest.builder()
              .bucket(config.getBucketName())
              .key(key)
              .uploadId(uploadId)
              .multipartUpload(completedMultipartUpload)
              .build();

      s3Client.completeMultipartUpload(completeRequest);
      log.info("Completed multipart upload for key: {}", key);
      return key;
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.error("Failed to complete multipart upload for key: {}", key, e);
      throw new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, "Completion failed");
    }
  }

  @Override
  public void abortMultipartUpload(String uploadId, String key) {
    try {
      AbortMultipartUploadRequest abortRequest =
          AbortMultipartUploadRequest.builder()
              .bucket(config.getBucketName())
              .key(key)
              .uploadId(uploadId)
              .build();
      s3Client.abortMultipartUpload(abortRequest);
      log.info("Aborted multipart upload for key: {}", key);
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.warn(
          "Failed to abort multipart upload for key: {}. It may have already completed or failed.",
          key,
          e);
    }
  }

  @Override
  public String getPublicUrl(String source) {
    String publicUrl = config.getPublicUrl();
    if (publicUrl == null || publicUrl.isEmpty()) {
      return source;
    }
    return publicUrl.endsWith(StorageConstants.PATH_DELIMITER)
        ? publicUrl + source
        : publicUrl + StorageConstants.PATH_DELIMITER + source;
  }

  @Override
  public java.util.Optional<String> generatePresignedUrl(String source) {
    try {
      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(config.getBucketName()).key(source).build();

      GetObjectPresignRequest getObjectPresignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(Duration.ofMinutes(60))
              .getObjectRequest(getObjectRequest)
              .build();

      return java.util.Optional.of(
          s3Presigner.presignGetObject(getObjectPresignRequest).url().toString());
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.error("Failed to generate pre-signed URL for key: {}", source, e);
      return java.util.Optional.empty();
    }
  }

  @Override
  public void delete(String source) {
    try {
      DeleteObjectRequest deleteObjReq =
          DeleteObjectRequest.builder().bucket(config.getBucketName()).key(source).build();
      s3Client.deleteObject(deleteObjReq);
      log.info("Deleted file from R2/S3: {}", source);
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.error("Failed to delete file from R2/S3", e);
      throw new RuntimeException("Failed to delete file from storage", e);
    }
  }

  @Override
  public String copy(String source, String destination) {
    try {
      CopyObjectRequest copyReq =
          CopyObjectRequest.builder()
              .sourceBucket(config.getBucketName())
              .sourceKey(source)
              .destinationBucket(config.getBucketName())
              .destinationKey(destination)
              .build();

      s3Client.copyObject(copyReq);
      log.info("Copied file from {} to {}", source, destination);
      return destination;
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.error("Failed to copy file from {} to {}", source, destination, e);
      throw new RuntimeException("Failed to copy file in storage", e);
    }
  }

  @Override
  public String move(String source, String destination) {
    // S3 does not have a native "rename". We must Copy then Delete.
    String newSource = copy(source, destination);
    delete(source);
    log.info("Moved file from {} to {}", source, destination);
    return newSource;
  }

  @Override
  public String createFolder(String folderPath) {
    if (!folderPath.endsWith(StorageConstants.PATH_DELIMITER)) {
      folderPath = folderPath + StorageConstants.PATH_DELIMITER;
    }
    try {
      PutObjectRequest putObjReq =
          PutObjectRequest.builder().bucket(config.getBucketName()).key(folderPath).build();

      s3Client.putObject(putObjReq, RequestBody.empty());
      log.info("Created virtual folder in R2/S3: {}", folderPath);
      return folderPath;
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.error("Failed to create folder in R2/S3: {}", folderPath, e);
      throw new RuntimeException("Failed to create folder in storage", e);
    }
  }

  @Override
  public InputStream download(String source) {
    try {
      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(config.getBucketName()).key(source).build();

      return s3Client.getObject(getObjectRequest);
    } catch (software.amazon.awssdk.core.exception.SdkException e) {
      log.error("Failed to download asset from R2: {}", source, e);
      throw new RuntimeException("Storage download failed", e);
    }
  }

  @Override
  public String getProviderName() {
    return StorageConstants.PROVIDER_R2;
  }
}
