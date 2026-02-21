package com.chamrong.iecommerce.auth.application.dto;

import com.chamrong.iecommerce.auth.domain.TenantPreferences;

public record TenantPreferencesResponse(
    String logoUrl, String primaryColor, String secondaryColor, String fontFamily) {
  public static TenantPreferencesResponse from(TenantPreferences preferences) {
    if (preferences == null) {
      return new TenantPreferencesResponse(null, "#1a73e8", "#f8f9fa", "Inter, Roboto, sans-serif");
    }
    return new TenantPreferencesResponse(
        preferences.getLogoUrl(),
        preferences.getPrimaryColor(),
        preferences.getSecondaryColor(),
        preferences.getFontFamily());
  }
}
