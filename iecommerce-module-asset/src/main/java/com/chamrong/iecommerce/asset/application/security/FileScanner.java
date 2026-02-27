package com.chamrong.iecommerce.asset.application.security;

import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import java.io.InputStream;

/**
 * Port for file scanning operations. Implementations can wrap ClamAV, Snyk, or other scanning
 * services.
 */
public interface FileScanner {
  /**
   * Scans the given input stream for malicious content.
   *
   * @param inputStream The content to scan.
   * @param fileName The name of the file for reference in logs.
   * @throws SecurityValidationException if malicious content is detected.
   */
  void scan(InputStream inputStream, String fileName) throws SecurityValidationException;
}
