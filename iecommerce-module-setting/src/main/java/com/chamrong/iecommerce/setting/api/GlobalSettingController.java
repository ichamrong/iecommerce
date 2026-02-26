package com.chamrong.iecommerce.setting.api;

import com.chamrong.iecommerce.setting.application.SettingService;
import com.chamrong.iecommerce.setting.application.dto.SettingRequest;
import com.chamrong.iecommerce.setting.application.dto.SettingResponse;
import com.chamrong.iecommerce.setting.domain.SettingCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-level global setting management.
 *
 * <p>Only platform super-admins can manage global settings. All operations require the {@code
 * settings:manage} permission.
 *
 * <p>Base path: {@code /api/v1/admin/settings}
 */
@Tag(name = "Admin — Global Settings", description = "Platform-wide configuration management")
@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('settings:manage')")
public class GlobalSettingController {

  private final SettingService settingService;

  @Operation(
      summary = "List all global settings",
      description =
          "Returns all platform-wide settings. Secret values are masked with ***."
              + " Supports optional category filter.")
  @GetMapping
  public List<SettingResponse> listAll(@RequestParam(required = false) SettingCategory category) {
    if (category != null) {
      return settingService.getGlobalSettingsByCategory(category);
    }
    return settingService.getAllGlobalSettings();
  }

  @Operation(
      summary = "Upsert a global setting",
      description =
          "Creates or updates a platform-wide setting by key. Supports partial updates — only"
              + " non-null fields are applied.")
  @PutMapping("/{key}")
  public SettingResponse upsert(@PathVariable String key, @RequestBody SettingRequest request) {
    return settingService.upsertGlobalSetting(key, request);
  }

  @Operation(
      summary = "Delete a global setting",
      description = "Permanently removes a platform-wide setting by key.")
  @DeleteMapping("/{key}")
  public ResponseEntity<Void> delete(@PathVariable String key) {
    settingService.deleteGlobalSetting(key);
    return ResponseEntity.noContent().build();
  }
}
