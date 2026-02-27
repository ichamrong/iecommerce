package com.chamrong.iecommerce.asset.application.metadata;

import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Extracts metadata from video files using the metadata-extractor library. Supports MP4, MOV, and
 * other common formats.
 */
@Slf4j
@Component
public class VideoMetadataExtractor implements MetadataExtractor {

  @Override
  public boolean supports(String mimeType) {
    return mimeType != null && mimeType.startsWith("video/");
  }

  @Override
  public Map<String, Object> extract(InputStream inputStream, String mimeType) {
    Map<String, Object> metadata = new HashMap<>();
    try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
      bis.mark(StorageConstants.BUFFER_SIZE);
      Metadata meta = ImageMetadataReader.readMetadata(bis);

      for (Directory directory : meta.getDirectories()) {
        for (Tag tag : directory.getTags()) {
          String tagName = tag.getTagName().toLowerCase().replace(" ", "");
          // Filter for useful video tags
          if (tagName.contains("duration")
              || tagName.contains("width")
              || tagName.contains("height")
              || tagName.contains("frame")
              || tagName.contains("bitrate")) {
            metadata.put(tagName, tag.getDescription());
          }
        }
      }

      // Attempt to set a friendly type if possible
      FileType fileType = FileTypeDetector.detectFileType(bis);
      metadata.put("format", fileType.name());

    } catch (Exception e) {
      log.warn("Failed to extract video metadata: {}", e.getMessage());
    }
    return metadata;
  }
}
