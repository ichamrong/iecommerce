package com.chamrong.iecommerce.setting.api;

import com.chamrong.iecommerce.setting.application.SettingService;
import com.chamrong.iecommerce.setting.application.dto.SettingRequest;
import com.chamrong.iecommerce.setting.application.dto.SettingResponse;
import com.chamrong.iecommerce.setting.domain.SettingCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenant self-service setting management.
 *
 * <p>Each tenant can view and update their own store configuration. Secret values (passwords, API
 * keys) are always masked in responses.
 *
 * <p>Base path: {@code /api/v1/tenants/me/settings}
 */
@Tag(name = "Tenant — Settings", description = "Tenant self-service store configuration")
@RestController
@RequestMapping("/api/v1/tenants/me/settings")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('settings:read') or hasAuthority('settings:manage')")
public class TenantSettingController {

  private final SettingService settingService;

  @Operation(
      summary = "List my settings",
      description =
          "Returns all settings for the current tenant. Secret values are masked with ***."
              + " Supports optional category filter.")
  @GetMapping
  public List<SettingResponse> listMySettings(
      @RequestParam String tenantId,
      @RequestParam(required = false) SettingCategory category) {
    if (category != null) {
      return settingService.getTenantSettingsByCategory(tenantId, category);
    }
    return settingService.getTenantSettings(tenantId);
  }

  @Operation(
      summary = "Upsert a tenant setting",
      description =
          "Creates or updates a store-specific setting by key. Requires `settings:manage`.")
  @PutMapping("/{key}")
  @PreAuthorize("hasAuthority('settings:manage')")
  public SettingResponse upsert(
      @RequestParam String tenantId,
      @PathVariable String key,
      @RequestBody SettingRequest request) {
    return settingService.upsertTenantSetting(tenantId, key, request);
  }

  @Operation(
      summary = "Reset a tenant setting to default",
      description =
          "Removes the tenant override and restores the global platform default for the given key."
              + " If no global default exists, the setting is simply deleted.")
  @PostMapping("/{key}/reset")
  @PreAuthorize("hasAuthority('settings:manage')")
  public ResponseEntity<SettingResponse> resetToDefault(
      @RequestParam String tenantId, @PathVariable String key) {
    Optional<SettingResponse> result = settingService.resetTenantSettingToDefault(tenantId, key);
    return result.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
  }

  @Operation(
      summary = "Delete a tenant setting",
      description = "Permanently removes a tenant-specific setting override.")
  @DeleteMapping("/{key}")
  @PreAuthorize("hasAuthority('settings:manage')")
  public ResponseEntity<Void> delete(@RequestParam String tenantId, @PathVariable String key) {
    settingService.deleteTenantSetting(tenantId, key);
    return ResponseEntity.noContent().build();
  }
}
