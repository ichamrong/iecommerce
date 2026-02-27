package com.chamrong.iecommerce.asset.application.metadata;

import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

/** Extracts metadata from image files using standard ImageIO. */
@Slf4j
@Component
public class ImageMetadataExtractor implements MetadataExtractor {

  @Override
  public boolean supports(String mimeType) {
    return mimeType != null && mimeType.startsWith(StorageConstants.MIME_IMAGE_PREFIX);
  }

  @Override
  public Map<String, Object> extract(InputStream inputStream, String mimeType) {
    Map<String, Object> metadata = new HashMap<>();

    // Use a buffered stream so we can reset it if needed, or just read it once.
    // metadata-extractor and ImageIO both need to read the stream.
    try {
      byte[] bytes = inputStream.readAllBytes();

      // 1. Basic Dimensions & Placeholder via ImageIO/Thumbnailator
      try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bytes)) {
        BufferedImage image = ImageIO.read(bis);
        if (image != null) {
          metadata.put("width", image.getWidth());
          metadata.put("height", image.getHeight());
          metadata.put("aspectRatio", (double) image.getWidth() / image.getHeight());

          // Generate a tiny placeholder (16x16)
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          Thumbnails.of(image).size(16, 16).outputFormat("jpg").toOutputStream(os);
          String placeholder = Base64.getEncoder().encodeToString(os.toByteArray());
          metadata.put("placeholder", "data:image/jpeg;base64," + placeholder);
        }
      }

      // 2. EXIF & Advanced Metadata via metadata-extractor
      try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bytes)) {
        Metadata meta = ImageMetadataReader.readMetadata(bis);
        for (Directory directory : meta.getDirectories()) {
          for (Tag tag : directory.getTags()) {
            String tagName = tag.getTagName().toLowerCase().replace(" ", "");
            // Whitelist for common useful tags to avoid bloat
            if (isExifTagInteresting(tagName)) {
              metadata.put(tagName, tag.getDescription());
            }
          }
        }
      }
    } catch (Exception e) {
      log.warn("Failed to extract image metadata: {}", e.getMessage());
    }
    return metadata;
  }

  private boolean isExifTagInteresting(String tagName) {
    return tagName.contains("make")
        || tagName.contains("model")
        || tagName.contains("datetime")
        || tagName.contains("exposure")
        || tagName.contains("f-number")
        || tagName.contains("iso")
        || tagName.contains("gps")
        || tagName.contains("software")
        || tagName.contains("orientation");
  }
}
