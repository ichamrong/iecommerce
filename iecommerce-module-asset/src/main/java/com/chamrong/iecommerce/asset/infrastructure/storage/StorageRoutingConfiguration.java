package com.chamrong.iecommerce.asset.infrastructure.storage;

import com.chamrong.iecommerce.asset.domain.StorageProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iecommerce.storage")
@Data
public class StorageRoutingConfiguration {

  /** The active storage provider. E.g., R2 or TELEGRAM. */
  private StorageProvider provider = StorageProvider.R2;
}
