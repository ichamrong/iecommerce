package com.chamrong.iecommerce.asset.infrastructure.storage;

import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.StorageProvider;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class StorageRoutingService implements StorageService {

  private final List<StorageService> allServices;
  private final StorageRoutingConfiguration config;

  private final Map<String, StorageService> providers = new HashMap<>();

  @PostConstruct
  public void init() {
    allServices.stream()
        .filter(s -> !(s instanceof StorageRoutingService))
        .forEach(this::registerProvider);
    log.info("Storage routing initialized with providers: {}", providers.keySet());
  }

  private void registerProvider(StorageService service) {
    String providerName = service.getProviderName();
    StorageProvider provider = StorageProvider.fromKey(providerName);
    String key = provider.getKey();

    if (providers.containsKey(key)) {
      log.error("Duplicate storage provider registration detected: {}", key);
      throw new StorageException(
          AssetErrorCode.INVALID_STORAGE_PROVIDER,
          "Duplicate storage provider registration for key: " + key);
    }

    providers.put(key, service);

    // Unified alias registration using StorageProvider mapping
    for (String alias : provider.getAliases()) {
      if (providers.containsKey(alias)) {
        log.warn("Storage provider alias overlap detected and overridden for alias: {}", alias);
      }
      providers.put(alias, service);
    }

    log.info("Registered storage provider: {}", providerName);
  }

  @Override
  public String upload(String fileName, String contentType, InputStream inputStream, long size) {
    return getPrimaryService().upload(fileName, contentType, inputStream, size);
  }

  @Override
  public String initiateMultipartUpload(String fileName, String contentType) {
    return getPrimaryService().initiateMultipartUpload(fileName, contentType);
  }

  @Override
  public String uploadPart(
      String uploadId, String key, int partNumber, InputStream inputStream, long size) {
    return getPrimaryService().uploadPart(uploadId, key, partNumber, inputStream, size);
  }

  @Override
  public String completeMultipartUpload(String uploadId, String key, Map<Integer, String> parts) {
    return getPrimaryService().completeMultipartUpload(uploadId, key, parts);
  }

  @Override
  public void abortMultipartUpload(String uploadId, String key) {
    getPrimaryService().abortMultipartUpload(uploadId, key);
  }

  @Override
  public String getPublicUrl(String source) {
    return getPrimaryService().getPublicUrl(source);
  }

  @Override
  public Optional<String> generatePresignedUrl(String source) {
    return getPrimaryService().generatePresignedUrl(source);
  }

  @Override
  public void delete(String source) {
    getPrimaryService().delete(source);
  }

  @Override
  public String copy(String source, String destination) {
    return getPrimaryService().copy(source, destination);
  }

  @Override
  public String move(String source, String destination) {
    return getPrimaryService().move(source, destination);
  }

  @Override
  public String createFolder(String folderPath) {
    return getPrimaryService().createFolder(folderPath);
  }

  @Override
  public InputStream download(String source) {
    return getPrimaryService().download(source);
  }

  @Override
  public String getProviderName() {
    return StorageConstants.PROVIDER_ROUTER;
  }

  private StorageService getPrimaryService() {
    StorageProvider provider = config.getProvider();
    String key = provider.getKey();
    StorageService service = providers.get(key);

    if (service == null) {
      log.warn(
          "Storage provider '{}' not found, falling back to {}",
          key,
          StorageConstants.DEFAULT_PROVIDER);
      return providers.get(StorageConstants.DEFAULT_PROVIDER);
    }
    return service;
  }
}
