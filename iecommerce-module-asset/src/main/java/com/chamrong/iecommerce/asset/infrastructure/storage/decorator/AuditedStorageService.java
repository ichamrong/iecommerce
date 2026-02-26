package com.chamrong.iecommerce.asset.infrastructure.storage.decorator;

import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import java.io.InputStream;
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

  @Override
  public String upload(String fileName, String contentType, InputStream inputStream, long size) {
    return execute("upload", () -> delegate.upload(fileName, contentType, inputStream, size));
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
      log.info(
          "Completed storage operation: {} on provider: {} in {}ms",
          operation,
          provider,
          stopWatch.getTotalTimeMillis());
      return result;
    } catch (StorageException e) {
      // Re-throw our domain exception
      throw e;
    } catch (Exception e) {
      stopWatch.stop();
      log.error("Storage operation failed: {} on provider: {}", operation, provider, e);
      throw new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, e.getMessage());
    }
  }

  @FunctionalInterface
  private interface StorageOperation<T> {

    T perform() throws Exception;
  }
}
