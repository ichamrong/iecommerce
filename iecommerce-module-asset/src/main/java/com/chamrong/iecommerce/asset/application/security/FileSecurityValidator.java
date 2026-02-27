package com.chamrong.iecommerce.asset.application.security;

import com.chamrong.iecommerce.asset.domain.StorageConstants;
import com.chamrong.iecommerce.asset.domain.exception.AssetErrorCode;
import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validates file security using high-performance O(1) signature matching and path traversal
 * protection. Following bank-level security standards.
 */
@Slf4j
@Component
public class FileSecurityValidator {

  private static final Map<String, Predicate<byte[]>> SIGNATURE_VALIDATORS =
      Map.of(
          StorageConstants.EXT_JPG, FileSecurityValidator::isJpeg,
          StorageConstants.EXT_JPEG, FileSecurityValidator::isJpeg,
          StorageConstants.EXT_PNG, FileSecurityValidator::isPng,
          StorageConstants.EXT_PDF, FileSecurityValidator::isPdf);

  public void validate(String fileName, InputStream inputStream) {
    if (fileName == null || fileName.isBlank()) {
      throw new SecurityValidationException(
          AssetErrorCode.VALIDATION_ERROR, "File name cannot be empty");
    }

    // Path Traversal Protection
    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
      log.error("Possible path traversal attempt detected in fileName: {}", fileName);
      throw new SecurityValidationException(
          AssetErrorCode.PATH_TRAVERSAL_ATTEMPT, "Invalid characters in file name");
    }

    String extension = getExtension(fileName).toLowerCase();

    if (StorageConstants.DANGEROUS_EXTENSIONS.contains(extension)) {
      log.warn(
          "Blocked attempt to upload dangerous file type: extension={}, name={}",
          extension,
          fileName);
      throw new SecurityValidationException(
          AssetErrorCode.INSECURE_FILE,
          "Upload of this file type is restricted for security reasons.");
    }

    validateMagicNumbers(extension, inputStream);
  }

  private String getExtension(String fileName) {
    int lastIndexOf = fileName.lastIndexOf(StorageConstants.DOT);
    return (lastIndexOf == -1) ? "" : fileName.substring(lastIndexOf + 1);
  }

  private void validateMagicNumbers(String extension, InputStream stream) {
    Predicate<byte[]> validator = SIGNATURE_VALIDATORS.get(extension);
    if (validator == null) {
      return; // No signature validator registered for this extension
    }

    try {
      if (!stream.markSupported()) {
        log.warn(
            "InputStream does not support mark/reset. Magic number verification skipped for"
                + " extension: {}",
            extension);
        return;
      }

      stream.mark(8);
      byte[] header = stream.readNBytes(8);
      stream.reset();

      if (header.length < 4) {
        log.debug("File too small for signature validation, relying on extension only.");
        return;
      }

      if (!validator.test(header)) {
        log.error("Security violation: Magic number mismatch for extension {}", extension);
        throw new SecurityValidationException(
            AssetErrorCode.INVALID_MIME_TYPE,
            "File content does not match the declared " + extension.toUpperCase() + " extension.");
      }
    } catch (IOException e) {
      log.error("Failed to read file signature for validation", e);
      throw new SecurityValidationException(
          AssetErrorCode.INTERNAL_ERROR, "Could not validate file signature.");
    }
  }

  private static boolean isJpeg(byte[] h) {
    return h.length >= 2 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8;
  }

  private static boolean isPng(byte[] h) {
    return h.length >= 8
        && (h[0] & 0xFF) == 0x89
        && (h[1] & 0xFF) == 0x50
        && (h[2] & 0xFF) == 0x4E
        && (h[3] & 0xFF) == 0x47
        && (h[4] & 0xFF) == 0x0D
        && (h[5] & 0xFF) == 0x0A
        && (h[6] & 0xFF) == 0x1A
        && (h[7] & 0xFF) == 0x0A;
  }

  private static boolean isPdf(byte[] h) {
    return h.length >= 5
        && h[0] == 0x25
        && h[1] == 0x50
        && h[2] == 0x44
        && h[3] == 0x46
        && h[4] == 0x2D;
  }
}
