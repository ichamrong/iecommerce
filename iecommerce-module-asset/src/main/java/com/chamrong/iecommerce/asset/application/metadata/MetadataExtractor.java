package com.chamrong.iecommerce.asset.application.metadata;

import java.io.InputStream;
import java.util.Map;

/**
 * Interface for extracting metadata from various file types. Standardizes metadata storage in the
 * Asset entity.
 */
public interface MetadataExtractor {

  /**
   * Checks if this extractor supports the given MIME type.
   *
   * @param mimeType The MIME type to check.
   * @return true if supported, false otherwise.
   */
  boolean supports(String mimeType);

  /**
   * Extracts metadata from the given input stream.
   *
   * @param inputStream The file content stream.
   * @param mimeType The MIME type of the file.
   * @return A map of extracted metadata.
   */
  Map<String, Object> extract(InputStream inputStream, String mimeType);
}
