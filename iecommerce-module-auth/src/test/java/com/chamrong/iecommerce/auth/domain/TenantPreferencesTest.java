package com.chamrong.iecommerce.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TenantPreferencesTest {

  @Test
  void defaultsShouldProvideSafeBranding() {
    TenantPreferences prefs = new TenantPreferences();

    assertThat(prefs.getLogoUrl()).isNull();
    assertThat(prefs.getPrimaryColor()).isEqualTo("#1a73e8");
    assertThat(prefs.getSecondaryColor()).isEqualTo("#f8f9fa");
    assertThat(prefs.getFontFamily()).isEqualTo("Inter, Roboto, sans-serif");
  }

  @Test
  void settersShouldOverrideDefaults() {
    TenantPreferences prefs = new TenantPreferences();
    prefs.setLogoUrl("https://cdn.example.com/logo.png");
    prefs.setPrimaryColor("#000000");
    prefs.setSecondaryColor("#ffffff");
    prefs.setFontFamily("System UI");

    assertThat(prefs.getLogoUrl()).isEqualTo("https://cdn.example.com/logo.png");
    assertThat(prefs.getPrimaryColor()).isEqualTo("#000000");
    assertThat(prefs.getSecondaryColor()).isEqualTo("#ffffff");
    assertThat(prefs.getFontFamily()).isEqualTo("System UI");
  }
}
