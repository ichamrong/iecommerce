package com.chamrong.iecommerce.asset.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ImageProcessingService {

  public InputStream resize(InputStream input, int width, int height, String format)
      throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Thumbnails.of(input).size(width, height).outputFormat(format).toOutputStream(os);
    log.debug("Resized image to {}x{} format {}", width, height, format);
    return new ByteArrayInputStream(os.toByteArray());
  }

  public InputStream crop(InputStream input, int width, int height, String format)
      throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Thumbnails.of(input)
        .crop(Positions.CENTER) // Default to center crop
        .size(width, height)
        .outputFormat(format)
        .toOutputStream(os);
    log.debug("Cropped image to {}x{} format {}", width, height, format);
    return new ByteArrayInputStream(os.toByteArray());
  }
}
