package com.chamrong.iecommerce.setting.application;

import com.chamrong.iecommerce.setting.application.dto.SettingRequest;
import com.chamrong.iecommerce.setting.application.dto.SettingResponse;
import com.chamrong.iecommerce.setting.domain.GlobalSetting;
import com.chamrong.iecommerce.setting.domain.GlobalSettingRepository;
import com.chamrong.iecommerce.setting.domain.SettingCategory;
import com.chamrong.iecommerce.setting.domain.SettingDataType;
import com.chamrong.iecommerce.setting.domain.TenantSetting;
import com.chamrong.iecommerce.setting.domain.TenantSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for managing platform-wide and tenant-specific settings.
 *
 * <p>All value reads are masked automatically for secret settings before being returned to callers.
 */
@Service
@RequiredArgsConstructor
public class SettingService {

  private final GlobalSettingRepository globalSettingRepository;
  private final TenantSettingRepository tenantSettingRepository;

  // ── Global Settings ───────────────────────────────────────────────────────

  /** Returns the raw value of a global setting, or an empty Optional if not found. */
  @Transactional(readOnly = true)
  public Optional<String> getGlobalValue(String key) {
    return globalSettingRepository.findByKey(key).map(GlobalSetting::getValue);
  }

  /** Returns all global settings (values masked for secrets). */
  @Transactional(readOnly = true)
  public List<SettingResponse> getAllGlobalSettings() {
    return globalSettingRepository.findAll().stream()
        .map(s -> toGlobalResponse(s).masked())
        .toList();
  }

  /** Returns global settings filtered by category (values masked for secrets). */
  @Transactional(readOnly = true)
  public List<SettingResponse> getGlobalSettingsByCategory(SettingCategory category) {
    return globalSettingRepository.findByCategory(category).stream()
        .map(s -> toGlobalResponse(s).masked())
        .toList();
  }

  /**
   * Upsert a global setting.
   *
   * <p>If the key already exists, only non-null fields from {@code request} are applied.
   */
  @Transactional
  public SettingResponse upsertGlobalSetting(String key, SettingRequest request) {
    GlobalSetting setting = globalSettingRepository.findByKey(key).orElse(new GlobalSetting());
    setting.setKey(key);
    if (request.value() != null) setting.setValue(request.value());
    if (request.description() != null) setting.setDescription(request.description());
    if (request.category() != null) setting.setCategory(request.category());
    if (request.dataType() != null) setting.setDataType(request.dataType());
    if (request.secret() != null) setting.setSecret(request.secret());
    return toGlobalResponse(globalSettingRepository.save(setting)).masked();
  }

  /** Permanently removes a global setting by key. */
  @Transactional
  public void deleteGlobalSetting(String key) {
    globalSettingRepository
        .findByKey(key)
        .orElseThrow(() -> new EntityNotFoundException("Global setting not found: " + key));
    globalSettingRepository.deleteByKey(key);
  }

  // ── Tenant Settings ───────────────────────────────────────────────────────

  /** Returns the raw value of a tenant-specific setting, or empty if not found. */
  @Transactional(readOnly = true)
  public Optional<String> getTenantValue(String tenantId, String key) {
    return tenantSettingRepository.findByTenantIdAndKey(tenantId, key).map(TenantSetting::getValue);
  }

  /** Legacy overload — falls back to key-only lookup without tenant scoping. */
  @Transactional(readOnly = true)
  public Optional<String> getTenantValue(String key) {
    return tenantSettingRepository.findByKey(key).map(TenantSetting::getValue);
  }

  /** Returns all settings for a given tenant (values masked for secrets). */
  @Transactional(readOnly = true)
  public List<SettingResponse> getTenantSettings(String tenantId) {
    return tenantSettingRepository.findByTenantId(tenantId).stream()
        .map(s -> toTenantResponse(s).masked())
        .toList();
  }

  /** Returns settings for a tenant filtered by category (values masked for secrets). */
  @Transactional(readOnly = true)
  public List<SettingResponse> getTenantSettingsByCategory(
      String tenantId, SettingCategory category) {
    return tenantSettingRepository.findByTenantIdAndCategory(tenantId, category).stream()
        .map(s -> toTenantResponse(s).masked())
        .toList();
  }

  /**
   * Upsert a tenant-specific setting.
   *
   * <p>If the key already exists for this tenant, only non-null fields are applied.
   */
  @Transactional
  public SettingResponse upsertTenantSetting(String tenantId, String key, SettingRequest request) {
    TenantSetting setting =
        tenantSettingRepository.findByTenantIdAndKey(tenantId, key).orElse(new TenantSetting());
    setting.setTenantId(tenantId);
    setting.setKey(key);
    if (request.value() != null) setting.setValue(request.value());
    if (request.description() != null) setting.setDescription(request.description());
    if (request.category() != null) setting.setCategory(request.category());
    if (request.dataType() != null) setting.setDataType(request.dataType());
    if (request.secret() != null) setting.setSecret(request.secret());
    return toTenantResponse(tenantSettingRepository.save(setting)).masked();
  }

  /** Permanently removes a tenant-specific setting override. */
  @Transactional
  public void deleteTenantSetting(String tenantId, String key) {
    tenantSettingRepository
        .findByTenantIdAndKey(tenantId, key)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Tenant setting not found: key=" + key + ", tenant=" + tenantId));
    tenantSettingRepository.deleteByTenantIdAndKey(tenantId, key);
  }

  /**
   * Reset a tenant setting to the global platform default.
   *
   * <p>If no global default exists for the key, the tenant-specific entry is simply removed.
   */
  @Transactional
  public Optional<SettingResponse> resetTenantSettingToDefault(String tenantId, String key) {
    tenantSettingRepository.deleteByTenantIdAndKey(tenantId, key);
    return globalSettingRepository
        .findByKey(key)
        .map(
            global -> {
              var fallback = new TenantSetting();
              fallback.setTenantId(tenantId);
              fallback.setKey(key);
              fallback.setValue(global.getValue());
              fallback.setCategory(global.getCategory());
              fallback.setDataType(global.getDataType());
              fallback.setSecret(global.isSecret());
              fallback.setDescription("Reset to global default");
              return toTenantResponse(tenantSettingRepository.save(fallback)).masked();
            });
  }

  /**
   * Returns true if the feature flag for the given key is enabled for the tenant. Falls back to
   * global setting if no tenant override exists.
   */
  @Transactional(readOnly = true)
  public boolean isFeatureEnabled(String tenantId, String featureKey) {
    String value =
        getTenantValue(tenantId, featureKey).or(() -> getGlobalValue(featureKey)).orElse("false");
    return "true".equalsIgnoreCase(value.trim());
  }

  // ── Quota Convenience ─────────────────────────────────────────────────────

  /**
   * Returns the numeric quota for the given key for a tenant, falling back to the global default.
   *
   * @return Integer limit, or {@link Integer#MAX_VALUE} if no quota is configured (unlimited).
   */
  @Transactional(readOnly = true)
  public int getQuota(String tenantId, String quotaKey) {
    Optional<String> tenantVal = getTenantValue(tenantId, quotaKey);
    if (tenantVal.isPresent()) {
      return parseIntSafe(tenantVal.get(), Integer.MAX_VALUE);
    }
    return getGlobalValue(quotaKey)
        .map(v -> parseIntSafe(v, Integer.MAX_VALUE))
        .orElse(Integer.MAX_VALUE);
  }

  // ── Internal Helpers ──────────────────────────────────────────────────────

  private static int parseIntSafe(String value, int fallback) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private SettingResponse toGlobalResponse(GlobalSetting s) {
    return new SettingResponse(
        s.getId(),
        s.getKey(),
        s.getValue(),
        s.getDescription(),
        s.getCategory() != null ? s.getCategory() : SettingCategory.GENERAL,
        s.getDataType() != null ? s.getDataType() : SettingDataType.STRING,
        s.isSecret(),
        s.getCreatedAt(),
        s.getUpdatedAt());
  }

  private SettingResponse toTenantResponse(TenantSetting s) {
    return new SettingResponse(
        s.getId(),
        s.getKey(),
        s.getValue(),
        s.getDescription(),
        s.getCategory() != null ? s.getCategory() : SettingCategory.GENERAL,
        s.getDataType() != null ? s.getDataType() : SettingDataType.STRING,
        s.isSecret(),
        s.getCreatedAt(),
        s.getUpdatedAt());
  }
}
