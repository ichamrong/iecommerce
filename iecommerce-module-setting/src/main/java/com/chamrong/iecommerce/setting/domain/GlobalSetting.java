package com.chamrong.iecommerce.setting.domain;

import com.chamrong.iecommerce.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Platform-wide configuration entry — shared across all tenants.
 *
 * <p>Examples: {@code supported_languages}, {@code maintenance_mode}, {@code
 * platform_default_currency}.
 */
@Entity
@Table(name = "setting_global")
public class GlobalSetting extends BaseEntity {

  @Column(name = "setting_key", unique = true, nullable = false, length = 100)
  private String key;

  @Column(name = "setting_value", columnDefinition = "TEXT")
  private String value;

  @Column(name = "description", length = 500)
  private String description;

  /** Category for grouped retrieval (e.g., GENERAL, QUOTA, SECURITY). */
  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 50)
  private SettingCategory category = SettingCategory.GENERAL;

  /** Expected data type — used by clients to safely deserialize the value. */
  @Enumerated(EnumType.STRING)
  @Column(name = "data_type", nullable = false, length = 20)
  private SettingDataType dataType = SettingDataType.STRING;

  /**
   * When {@code true} this setting's value must be masked (replaced with {@code "***"}) in API
   * responses. Use for SMTP passwords, API keys, etc.
   */
  @Column(name = "is_secret", nullable = false)
  private boolean secret = false;

  public GlobalSetting() {}

  public GlobalSetting(
      String key, String value, SettingCategory category, SettingDataType dataType) {
    this.key = key;
    this.value = value;
    this.category = category;
    this.dataType = dataType;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public SettingCategory getCategory() {
    return category;
  }

  public void setCategory(SettingCategory category) {
    this.category = category;
  }

  public SettingDataType getDataType() {
    return dataType;
  }

  public void setDataType(SettingDataType dataType) {
    this.dataType = dataType;
  }

  public boolean isSecret() {
    return secret;
  }

  public void setSecret(boolean secret) {
    this.secret = secret;
  }

  public void updateValue(String newValue) {
    this.value = newValue;
  }
}
