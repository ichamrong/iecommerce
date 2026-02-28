package com.chamrong.iecommerce.auth.application.dto;

import com.chamrong.iecommerce.auth.domain.TenantPreferences;

public record TenantPreferencesResponse(
    String logoUrl, String primaryColor, String secondaryColor, String fontFamily) {
  public static TenantPreferencesResponse from(TenantPreferences preferences) {
    return new TenantPreferencesResponse(
        preferences.getLogoUrl(),
        preferences.getPrimaryColor(),
        preferences.getSecondaryColor(),
        preferences.getFontFamily());
  }
}
