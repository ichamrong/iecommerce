package com.chamrong.iecommerce.payment.infrastructure.bakong;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iecommerce.payment.bakong")
@Data
public class BakongConfiguration {
  private String bankName;
  private String merchantName;
  private String merchantCity;
  private String merchantId;
  private String accountId;
  private String acquiringBank;
  private String currency; // KHR or USD
}
