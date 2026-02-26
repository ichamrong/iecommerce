package com.chamrong.iecommerce.asset.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * Type-safe enumeration of supported storage providers. Using Enums over raw Strings improves
 * security and prevents configuration errors.
 */
@Getter
@RequiredArgsConstructor
public enum StorageProvider {
  R2(StorageConstants.PROVIDER_R2),
  GCS(StorageConstants.PROVIDER_GCS),
  TELEGRAM(StorageConstants.PROVIDER_TELEGRAM),
  ROUTER(StorageConstants.PROVIDER_ROUTER);

  private final String key;

  /**
   * Look up provider by key (case-insensitive).
   *
   * @param key The provider key string.
   * @return The matching StorageProvider.
   * @throws IllegalArgumentException if key is invalid.
   */
  public static StorageProvider fromKey(@Nullable String key) {
    if (key == null) {
      return R2; // Default fallback
    }
    String normalized = key.toLowerCase();
    for (StorageProvider provider : values()) {
      if (provider.key.equals(normalized)) {
        return provider;
      }
    }
    // Handle legacy aliases
    if (StorageConstants.ALIAS_S3.equals(normalized)) {
      return R2;
    }
    if (StorageConstants.ALIAS_GOOGLE.equals(normalized)) {
      return GCS;
    }

    throw new IllegalArgumentException("Unknown storage provider: " + key);
  }
}
