package com.chamrong.iecommerce.setting.api;

import com.chamrong.iecommerce.setting.application.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Clean API for checking feature flags. */
@Tag(name = "Feature Flags", description = "System-wide and tenant-specific feature toggles")
@RestController
@RequestMapping("/api/v1/settings/features")
@RequiredArgsConstructor
public class FeatureFlagController {

  private final SettingService settingService;

  @Operation(summary = "Check if a feature is enabled for a tenant")
  @GetMapping("/{featureKey}")
  public ResponseEntity<Map<String, Boolean>> isEnabled(
      @RequestParam String tenantId, @PathVariable String featureKey) {
    boolean enabled = settingService.isFeatureEnabled(tenantId, featureKey);
    return ResponseEntity.ok(Map.of("enabled", enabled));
  }
}
