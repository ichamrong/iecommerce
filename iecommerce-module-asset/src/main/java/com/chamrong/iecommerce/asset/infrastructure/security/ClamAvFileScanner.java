package com.chamrong.iecommerce.asset.infrastructure.security;

import com.chamrong.iecommerce.asset.application.security.FileScanner;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import fi.solita.clamav.ClamAVClient;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Real implementation of {@link FileScanner} using ClamAV daemon. Connects to ClamAV over TCP/IP to
 * perform high-performance virus scanning.
 */
@Slf4j
@Primary
@Component
public class ClamAvFileScanner implements FileScanner {

  private final ClamAVClient client;

  public ClamAvFileScanner(
      @Value("${app.asset.security.clamav.host:localhost}") String host,
      @Value("${app.asset.security.clamav.port:3310}") int port) {
    this.client = new ClamAVClient(host, port);
  }

  @Override
  public void scan(InputStream inputStream, String fileName) throws SecurityValidationException {
    try {
      log.debug("Sending file {} to ClamAV for scanning...", fileName);
      byte[] reply = client.scan(inputStream);

      if (!ClamAVClient.isCleanReply(reply)) {
        String result = new String(reply).trim();
        log.warn("SECURITY ALERT: Malicious content detected in file {}: {}", fileName, result);
        throw new SecurityValidationException(
            AssetErrorCode.INSECURE_FILE,
            "Security violation: The file " + fileName + " contains malicious content.");
      }

      log.info("Virus scan passed for file: {}", fileName);
    } catch (IOException e) {
      log.error("ClamAV infrastructure failure during scan of file: {}", fileName, e);
      // In bank-level hardening, we fail closed (secure by default)
      throw new SecurityValidationException(
          AssetErrorCode.INTERNAL_ERROR,
          "Virus scanning service is unavailable. Upload aborted for security.");
    }
  }
}
