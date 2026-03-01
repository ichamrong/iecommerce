package com.chamrong.iecommerce.auth.domain;

import lombok.Getter;
import lombok.Setter;

/** Tenant storefront preferences (pure domain value object — no JPA). */
@Getter
@Setter
public class TenantPreferences {
  private String logoUrl;

  // Default fallback to the global modern Apple/Google Hybrid branding
  private String primaryColor = "#1a73e8";
  private String secondaryColor = "#f8f9fa";
  private String fontFamily = "Inter, Roboto, sans-serif";
}
