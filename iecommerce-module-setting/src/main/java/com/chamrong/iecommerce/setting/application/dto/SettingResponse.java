package com.chamrong.iecommerce.setting.application.dto;

import com.chamrong.iecommerce.setting.domain.SettingCategory;
import com.chamrong.iecommerce.setting.domain.SettingDataType;
import java.time.Instant;

/**
 * API response for a single setting entry.
 *
 * <p>The {@code value} field will be {@code "***"} when {@code secret} is {@code true}, preventing
 * credentials from being exposed over the wire.
 */
public record SettingResponse(
    Long id,
    String key,
    /** Masked with {@code "***"} for secret settings. */
    String value,
    String description,
    SettingCategory category,
    SettingDataType dataType,
    boolean secret,
    Instant createdAt,
    Instant updatedAt) {

  private static final String MASKED = "***";

  /** Returns a safe copy of this response where the value is masked when the setting is secret. */
  public SettingResponse masked() {
    if (secret) {
      return new SettingResponse(
          id, key, MASKED, description, category, dataType, true, createdAt, updatedAt);
    }
    return this;
  }
}
