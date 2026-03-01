package com.chamrong.iecommerce.auth.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA embeddable for tenant preferences. Domain value object is {@link
 * com.chamrong.iecommerce.auth.domain.TenantPreferences}.
 */
@Embeddable
@Getter
@Setter
public class TenantPreferencesEmbeddable {
  @Column(name = "logo_url")
  private String logoUrl;

  @Column(name = "primary_color")
  private String primaryColor = "#1a73e8";

  @Column(name = "secondary_color")
  private String secondaryColor = "#f8f9fa";

  @Column(name = "font_family")
  private String fontFamily = "Inter, Roboto, sans-serif";
}
