package com.chamrong.iecommerce.asset.application.metadata;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Composite extractor that delegates to all registered {@link MetadataExtractor} beans. */
@Component
@RequiredArgsConstructor
public class CompositeMetadataExtractor {

  private final List<MetadataExtractor> extractors;

  /**
   * Extracts metadata by finding the first extractor that supports the given MIME type.
   *
   * @param inputStream The file content stream.
   * @param mimeType The MIME type of the file.
   * @return A map of extracted metadata, or empty map if no extractor supports it.
   */
  public Map<String, Object> extract(InputStream inputStream, String mimeType) {
    return extractors.stream()
        .filter(e -> e.supports(mimeType))
        .findFirst()
        .map(e -> e.extract(inputStream, mimeType))
        .orElse(Collections.emptyMap());
  }
}
