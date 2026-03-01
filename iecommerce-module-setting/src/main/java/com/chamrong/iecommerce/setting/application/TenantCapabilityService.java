package com.chamrong.iecommerce.setting.application;

import com.chamrong.iecommerce.common.security.CapabilityDeniedException;
import com.chamrong.iecommerce.common.security.CapabilityGate;
import com.chamrong.iecommerce.setting.domain.VerticalMode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Enforces tenant capability by vertical mode and enabled modules. Uses tenant setting {@code
 * vertical_mode} (values: ECOMMERCE, POS, ACCOMMODATION, HYBRID). If module is not in the allowed
 * set for that vertical, throws {@link CapabilityDeniedException} with {@link
 * CapabilityDeniedException#MODULE_DISABLED}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantCapabilityService implements CapabilityGate {

  /** Tenant setting key for vertical mode. */
  public static final String KEY_VERTICAL_MODE = "vertical_mode";

  private final SettingService settingService;

  @Override
  public void requireModule(String tenantId, String module) {
    if (tenantId == null || tenantId.isBlank()) {
      throw new CapabilityDeniedException(
          "Tenant context required", CapabilityDeniedException.MODULE_DISABLED);
    }
    if (module == null || module.isBlank()) {
      return;
    }
    VerticalMode mode = resolveVerticalMode(tenantId);
    if (!mode.allowsModule(module)) {
      log.debug("Module {} not allowed for tenant {} (vertical={})", module, tenantId, mode);
      throw new CapabilityDeniedException(
          "Module not allowed for this tenant: " + module,
          CapabilityDeniedException.MODULE_DISABLED);
    }
  }

  /** Resolves the tenant's vertical mode from settings; default ECOMMERCE. */
  public VerticalMode resolveVerticalMode(String tenantId) {
    Optional<String> value = settingService.getTenantValue(tenantId, KEY_VERTICAL_MODE);
    return VerticalMode.from(value.orElse(null));
  }
}
