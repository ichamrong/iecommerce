package com.chamrong.iecommerce.auth.infrastructure.init;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.kong")
public class KongProperties {
  private String adminUrl;
  private String upstreamUrl;
}
