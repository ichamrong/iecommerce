package com.chamrong.iecommerce.asset.infrastructure.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iecommerce.storage.telegram")
@Data
public class TelegramStorageConfiguration {
  private String botToken;
  private String chatId;
  private String apiUrl = "https://api.telegram.org";
}
