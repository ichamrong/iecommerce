package com.chamrong.iecommerce.asset.infrastructure.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.storage.gcs")
public class GoogleCloudStorageConfiguration {

  private String projectId;
  @org.springframework.lang.Nullable private String credentialsFilePath;
  private String bucketName;

  @Bean
  public Storage googleCloudStorage() throws IOException {
    log.info("Initializing Google Cloud Storage for project: {}", projectId);

    String path = credentialsFilePath;
    if (path == null || path.isBlank()) {
      log.warn(
          "No GCS credentials file path provided. Attempting to use default application"
              + " credentials.");
      return StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    }

    try (FileInputStream credentialsStream = new FileInputStream(path)) {
      GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
      return StorageOptions.newBuilder()
          .setCredentials(credentials)
          .setProjectId(projectId)
          .build()
          .getService();
    }
  }
}
