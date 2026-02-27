package com.chamrong.iecommerce.asset.infrastructure.storage.decorator;

import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import com.chamrong.iecommerce.common.event.EventDispatcher;
import com.chamrong.iecommerce.common.event.StorageOperationEvent;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

/**
 * Decorator for {@link StorageService} that adds auditing, operation timing, and unified exception
 * wrapping. Following Clean Code and Decorator pattern.
 */
@Slf4j
@RequiredArgsConstructor
public class AuditedStorageService implements StorageService {

  private final StorageService delegate;
  private final EventDispatcher eventDispatcher;

  @Override
  public String upload(String fileName, String contentType, InputStream inputStream, long size) {
    return execute("upload", () -> delegate.upload(fileName, contentType, inputStream, size));
  }

  @Override
  public String initiateMultipartUpload(String fileName, String contentType) {
    return execute(
        "initiateMultipartUpload", () -> delegate.initiateMultipartUpload(fileName, contentType));
  }

  @Override
  public String uploadPart(
      String uploadId, String key, int partNumber, InputStream inputStream, long size) {
    return execute(
        "uploadPart", () -> delegate.uploadPart(uploadId, key, partNumber, inputStream, size));
  }

  @Override
  public String completeMultipartUpload(String uploadId, String key, Map<Integer, String> parts) {
    return execute(
        "completeMultipartUpload", () -> delegate.completeMultipartUpload(uploadId, key, parts));
  }

  @Override
  public void abortMultipartUpload(String uploadId, String key) {
    execute(
        "abortMultipartUpload",
        () -> {
          delegate.abortMultipartUpload(uploadId, key);
          return null;
        });
  }

  @Override
  public void delete(String source) {
    execute(
        "delete",
        () -> {
          delegate.delete(source);
          return null;
        });
  }

  @Override
  public String copy(String source, String destination) {
    return execute("copy", () -> delegate.copy(source, destination));
  }

  @Override
  public String move(String source, String destination) {
    return execute("move", () -> delegate.move(source, destination));
  }

  @Override
  public String createFolder(String folderPath) {
    return execute("createFolder", () -> delegate.createFolder(folderPath));
  }

  @Override
  public String getPublicUrl(String source) {
    // URL generation is usually fast and local, but we still wrap for consistency
    return execute("getPublicUrl", () -> delegate.getPublicUrl(source));
  }

  @Override
  public Optional<String> generatePresignedUrl(String source) {
    return execute("generatePresignedUrl", () -> delegate.generatePresignedUrl(source));
  }

  @Override
  public InputStream download(String source) {
    return execute("download", () -> delegate.download(source));
  }

  @Override
  public String getProviderName() {
    return delegate.getProviderName();
  }

  /** Functional helper to wrap operations with auditing and uniform exception handling. */
  private <T> T execute(String operation, StorageOperation<T> action) {
    String provider = delegate.getProviderName();
    log.debug("Starting storage operation: {} on provider: {}", operation, provider);

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      T result = action.perform();
      stopWatch.stop();
      long duration = stopWatch.getTotalTimeMillis();

      log.info(
          "Completed storage operation: {} on provider: {} in {}ms", operation, provider, duration);

      eventDispatcher.dispatch(
          new StorageOperationEvent(
              provider,
              operation,
              null, // Source would be added if available in context or params
              duration,
              "SUCCESS",
              null,
              Map.of("delegate", delegate.getClass().getSimpleName()),
              Instant.now()));

      return result;
    } catch (StorageException e) {
      stopWatch.stop();
      eventDispatcher.dispatch(
          new StorageOperationEvent(
              provider,
              operation,
              null,
              stopWatch.getTotalTimeMillis(),
              "FAILURE",
              e.getMessage(),
              Map.of("errorCode", e.getErrorCode().getCode()),
              Instant.now()));
      throw e;
    } catch (Exception e) {
      stopWatch.stop();
      eventDispatcher.dispatch(
          new StorageOperationEvent(
              provider,
              operation,
              null,
              stopWatch.getTotalTimeMillis(),
              "FAILURE",
              e.getMessage(),
              Collections.emptyMap(),
              Instant.now()));
      log.error("Storage operation failed: {} on provider: {}", operation, provider, e);
      throw new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, e.getMessage());
    }
  }

  @FunctionalInterface
  private interface StorageOperation<T> {

    T perform() throws Exception;
  }
}
