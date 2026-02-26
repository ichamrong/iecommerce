package com.chamrong.iecommerce.setting.application.dto;

import com.chamrong.iecommerce.setting.domain.SettingCategory;
import com.chamrong.iecommerce.setting.domain.SettingDataType;

/**
 * Request payload for creating or updating a setting.
 *
 * <p>{@code value} is required. All other fields are optional on update — {@code null} means "keep
 * the existing value".
 */
public record SettingRequest(
    String value,
    String description,
    SettingCategory category,
    SettingDataType dataType,
    Boolean secret) {}
