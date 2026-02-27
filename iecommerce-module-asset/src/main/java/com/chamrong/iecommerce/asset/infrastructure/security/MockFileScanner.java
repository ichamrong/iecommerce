package com.chamrong.iecommerce.asset.infrastructure.security;

import com.chamrong.iecommerce.asset.application.security.FileScanner;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of a file scanner. In a real scenario, this would call an external API or a
 * local ClamAV instance.
 */
@Slf4j
@Component
public class MockFileScanner implements FileScanner {

  @Override
  public void scan(InputStream inputStream, String fileName) throws SecurityValidationException {
    log.info("Scanning file for viruses: {}", fileName);

    // Simulating a scan. In a real mock, we might trigger a failure for specific filenames.
    if (fileName.toLowerCase().contains("malware") || fileName.toLowerCase().contains("virus")) {
      log.error("Malicious content detected in file: {}", fileName);
      throw new SecurityValidationException(
          AssetErrorCode.INSECURE_FILE, "Malicious content detected during virus scan.");
    }

    log.info("File scan completed successfully: {}", fileName);
  }
}
