package com.chamrong.iecommerce.asset.infrastructure.storage.decorator;

import com.chamrong.iecommerce.asset.domain.StorageService;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Decorator for {@link StorageService} that adds Redis caching for storage URLs. Minimizes
 * expensive pre-signed URL generation and provider calls. Following Decorator pattern for
 * non-intrusive scalability.
 */
@Slf4j
@RequiredArgsConstructor
public class CachingStorageService implements StorageService {

  private final StorageService delegate;
  private final CacheManager cacheManager;

  private static final String CACHE_NAME = "storage_urls";

  @Override
  public String upload(String fileName, String contentType, InputStream inputStream, long size) {
    String result = delegate.upload(fileName, contentType, inputStream, size);
    evict(result);
    return result;
  }

  @Override
  public String initiateMultipartUpload(String fileName, String contentType) {
    return delegate.initiateMultipartUpload(fileName, contentType);
  }

  @Override
  public String uploadPart(
      String uploadId, String key, int partNumber, InputStream inputStream, long size) {
    return delegate.uploadPart(uploadId, key, partNumber, inputStream, size);
  }

  @Override
  public String completeMultipartUpload(String uploadId, String key, Map<Integer, String> parts) {
    String result = delegate.completeMultipartUpload(uploadId, key, parts);
    evict(result);
    return result;
  }

  @Override
  public void abortMultipartUpload(String uploadId, String key) {
    delegate.abortMultipartUpload(uploadId, key);
  }

  @Override
  public String getPublicUrl(String source) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      String cachedUrl = cache.get(source, String.class);
      if (cachedUrl != null) {
        log.debug("Cache hit for storage URL: {}", source);
        return cachedUrl;
      }
    }

    String url = delegate.getPublicUrl(source);
    if (cache != null && url != null) {
      cache.put(source, url);
    }
    return url;
  }

  @Override
  public Optional<String> generatePresignedUrl(String source) {
    return delegate.generatePresignedUrl(source);
  }

  @Override
  public void delete(String source) {
    delegate.delete(source);
    evict(source);
  }

  @Override
  public String copy(String source, String destination) {
    String result = delegate.copy(source, destination);
    evict(source);
    evict(destination);
    return result;
  }

  @Override
  public String move(String source, String destination) {
    String result = delegate.move(source, destination);
    evict(source);
    evict(destination);
    return result;
  }

  @Override
  public String createFolder(String folderPath) {
    return delegate.createFolder(folderPath);
  }

  @Override
  public InputStream download(String source) {
    // Download logic is streaming, we don't cache the stream itself.
    return delegate.download(source);
  }

  @Override
  public String getProviderName() {
    return delegate.getProviderName();
  }

  private void evict(String source) {
    if (source == null) return;
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      cache.evict(source);
      log.debug("Evicted storage cache for: {}", source);
    }
  }
}
