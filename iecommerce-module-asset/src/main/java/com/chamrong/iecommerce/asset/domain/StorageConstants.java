package com.chamrong.iecommerce.asset.domain;

/**
 * Centralized constants for storage provider names, URI schemes, and MIME types. Helps prevent
 * typos and aligns with SonarQube's Magic Strings rule.
 */
public final class StorageConstants {

  private StorageConstants() {
    // Utility class
  }

  // ── Storage Provider Keys ────────────────────────────────────────────────
  public static final String PROVIDER_R2 = "r2";
  public static final String PROVIDER_GCS = "gcs";
  public static final String PROVIDER_TELEGRAM = "telegram";
  public static final String DEFAULT_PROVIDER = PROVIDER_R2;

  // ── Storage Aliases ──────────────────────────────────────────────────────
  public static final String ALIAS_S3 = "s3";
  public static final String ALIAS_GOOGLE = "google";
  public static final String PROVIDER_ROUTER = "router";

  // ── Telegram Specifics ───────────────────────────────────────────────────
  public static final String TG_SCHEME = "tg://";
  public static final String TG_DELIMITER = "::";
  public static final String TG_BOT_PREFIX = "/bot";
  public static final String TG_FILE_PREFIX = "/file/bot";
  public static final String TG_METHOD_SEND_DOCUMENT = "/sendDocument";
  public static final String TG_METHOD_DELETE_MESSAGE = "/deleteMessage?chat_id=%s&message_id=%s";
  public static final String TG_METHOD_GET_FILE = "/getFile?file_id=";
  public static final String PATH_DELIMITER = "/";

  // ── Asset API Paths ──────────────────────────────────────────────────────
  public static final String API_V1_ASSETS = "/api/v1/assets";
  public static final String API_PROXY_PREFIX = API_V1_ASSETS + "/proxy/";
  public static final String DOWNLOAD_SUFFIX = "/download";

  // ── HTTP Headers & Values ────────────────────────────────────────────────
  public static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
  public static final String HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy";
  public static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
  public static final String VALUE_NOSNIFF = "nosniff";

  // ── Common MIME Types ────────────────────────────────────────────────────
  public static final String MIME_OCTET_STREAM = "application/octet-stream";
  public static final String MIME_DIRECTORY = "application/x-directory";
  public static final String MIME_ZIP = "application/zip";
  public static final String MIME_IMAGE_PREFIX = "image/";

  // ── File Extensions ──────────────────────────────────────────────────────
  public static final String EXT_JPG = "jpg";
  public static final String EXT_JPEG = "jpeg";
  public static final String EXT_PNG = "png";
  public static final String EXT_PDF = "pdf";
  public static final String EXT_ZIP = "zip";

  public static final java.util.List<String> DANGEROUS_EXTENSIONS =
      java.util.Arrays.asList(
          "exe", "sh", "bat", "cmd", "msi", "js", "vbs", "ps1", "jsp", "php", "pl", "py");

  // ── Common Values ────────────────────────────────────────────────────────
  public static final String UNKNOWN = "unknown";
  public static final String COPY_PREFIX = "Copy of ";
  public static final String DOT = ".";
  public static final String KEY_SEPARATOR = "-";
  public static final int BUFFER_SIZE = 8 * 1024; // 8KB
}
