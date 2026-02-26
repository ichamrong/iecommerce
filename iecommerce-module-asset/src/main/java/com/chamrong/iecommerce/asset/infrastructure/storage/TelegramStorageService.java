package com.chamrong.iecommerce.asset.infrastructure.storage;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.StorageService;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.StorageException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TelegramStorageService implements StorageService {

  private final TelegramStorageConfiguration config;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public String upload(String fileName, String contentType, InputStream inputStream, long size) {
    String url = buildBotUrl(StorageConstants.TG_METHOD_SEND_DOCUMENT);

    if (inputStream == null) {
      throw new StorageException(
          AssetErrorCode.VALIDATION_ERROR, "Upload InputStream cannot be null");
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("chat_id", config.getChatId());

    // Create an InputStreamResource that reports the filename
    body.add(
        "document",
        new InputStreamResource(inputStream) {
          @Override
          public String getFilename() {
            return fileName;
          }

          @Override
          public long contentLength() {
            return size;
          }
        });

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    try {
      String response = restTemplate.postForObject(url, requestEntity, String.class);
      JsonNode root = objectMapper.readTree(response);

      if (root.path("ok").asBoolean()) {
        JsonNode result = root.path("result");
        String fileId = result.path("document").path("file_id").asText();
        long messageId = result.path("message_id").asLong();
        log.info("Uploaded asset to Telegram: fileId={}, messageId={}", fileId, messageId);
        return StorageConstants.TG_SCHEME + fileId + StorageConstants.TG_DELIMITER + messageId;
      } else {
        String error = root.path("description").asText();
        log.error("Telegram upload failed: {}", error);
        throw new RuntimeException("Telegram storage error: " + error);
      }
    } catch (IOException | RuntimeException e) {
      log.error("Failed to upload asset to Telegram", e);
      throw new StorageException(AssetErrorCode.STORAGE_OPERATION_FAILED, "Storage upload failed: " + e.getMessage());
    }
  }

  @Override
  public String getPublicUrl(String source) {
    if (!source.startsWith(StorageConstants.TG_SCHEME)) {
      return source; // Not a telegram source
    }

    // Handle both old format (tg://file_id) and new format (tg://file_id::message_id)
    return StorageConstants.API_PROXY_PREFIX + source;
  }

  @Override
  public void delete(String source) {
    if (!source.startsWith(StorageConstants.TG_SCHEME)
        || !source.contains(StorageConstants.TG_DELIMITER)) {
      log.warn(
          "Cannot delete Telegram file: source format invalid or missing message_id: {}", source);
      return;
    }

    try {
      String[] parts =
          source
              .substring(StorageConstants.TG_SCHEME.length())
              .split(StorageConstants.TG_DELIMITER);
      String messageId = parts[1];
      String deleteUrl =
          buildBotUrl(
              String.format(
                  StorageConstants.TG_METHOD_DELETE_MESSAGE, config.getChatId(), messageId));

      restTemplate.getForObject(deleteUrl, String.class);
      log.info("Deleted Telegram message_id: {}", messageId);
    } catch (RuntimeException e) {
      log.error("Failed to delete message from Telegram: {}", source, e);
    }
  }

  @Override
  public String copy(String source, String destination) {
    throw new UnsupportedOperationException(
        "Copy operation is not supported by TelegramStorageService.");
  }

  @Override
  public String move(String source, String destination) {
    throw new UnsupportedOperationException(
        "Move operation is not supported by TelegramStorageService.");
  }

  @Override
  public String createFolder(String folderPath) {
    log.info(
        "Telegram storage does not support physical folders. Simulating folder: {}", folderPath);
    return folderPath;
  }

  @Override
  public InputStream download(String source) {
    String getFileUrl = buildGetFileUrl(source);

    try {
      String response = restTemplate.getForObject(getFileUrl, String.class);
      JsonNode root = objectMapper.readTree(response);

      if (root.path("ok").asBoolean()) {
        String filePath = root.path("result").path("file_path").asText();
        String downloadUrl = buildFileUrl(filePath);

        log.info("Downloading file from Telegram: {}", filePath);
        return java.net.URI.create(downloadUrl).toURL().openStream();
      } else {
        String error = root.path("description").asText();
        log.error("Telegram getFile failed: {}", error);
        throw new StorageException(
            AssetErrorCode.STORAGE_OPERATION_FAILED, "Telegram API error: " + error);
      }
    } catch (IOException | RuntimeException e) {
      log.error("Failed to download file from Telegram", e);
      throw new StorageException(
          AssetErrorCode.STORAGE_UNAVAILABLE, "Telegram download failed: " + e.getMessage());
    }
  }

  @Override
  public String getProviderName() {
    return StorageConstants.PROVIDER_TELEGRAM;
  }

  private String buildGetFileUrl(String source) {
    if (source == null || !source.startsWith(StorageConstants.TG_SCHEME)) {
      throw new StorageException(
          AssetErrorCode.VALIDATION_ERROR, "Invalid Telegram source URI: " + source);
    }
    return buildBotUrl(StorageConstants.TG_METHOD_GET_FILE + extractFileId(source));
  }

  private String extractFileId(String source) {
    String stripped = source.substring(StorageConstants.TG_SCHEME.length());
    return stripped.contains(StorageConstants.TG_DELIMITER)
        ? stripped.split(StorageConstants.TG_DELIMITER)[0]
        : stripped;
  }

  private String buildBotUrl(String method) {
    return config.getApiUrl() + StorageConstants.TG_BOT_PREFIX + config.getBotToken() + method;
  }

  private String buildFileUrl(String filePath) {
    return config.getApiUrl()
        + StorageConstants.TG_FILE_PREFIX
        + config.getBotToken()
        + StorageConstants.PATH_DELIMITER
        + filePath;
  }
}
