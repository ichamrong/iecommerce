package com.chamrong.iecommerce.payment.infrastructure.aba;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iecommerce.payment.aba")
@Data
public class ABAConfiguration {
  private String merchantId;
  private String apiKey;
  private String pushBackUrl;
  private String returnUrl;
  private String apiUrl;
}
