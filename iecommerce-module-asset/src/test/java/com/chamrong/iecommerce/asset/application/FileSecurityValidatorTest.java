package com.chamrong.iecommerce.asset.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chamrong.iecommerce.asset.application.security.FileSecurityValidator;
import com.chamrong.iecommerce.asset.domain.exception.SecurityValidationException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class FileSecurityValidatorTest {

  private final FileSecurityValidator validator = new FileSecurityValidator();

  @Test
  void testValidFile_Success() {
    assertDoesNotThrow(
        () -> validator.validate("document.txt", new ByteArrayInputStream(new byte[0])));
  }

  @Test
  void testPathTraversal_Detected() {
    assertThrows(
        SecurityValidationException.class, () -> validator.validate("../secret.txt", null));
    assertThrows(SecurityValidationException.class, () -> validator.validate("etc/passwd", null));
    assertThrows(
        SecurityValidationException.class, () -> validator.validate("sub\\dir\\file.txt", null));
  }

  @Test
  void testDangerousExtension_Blocked() {
    assertThrows(SecurityValidationException.class, () -> validator.validate("shell.sh", null));
    assertThrows(SecurityValidationException.class, () -> validator.validate("malware.exe", null));
  }

  @Test
  void testMagicNumberVerification_JpegSuccess() throws Exception {
    // JPEG magic number: FF D8
    byte[] content = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
    InputStream is = new BufferedInputStream(new ByteArrayInputStream(content));

    assertDoesNotThrow(() -> validator.validate("photo.jpg", is));
  }

  @Test
  void testMagicNumberVerification_PngSuccess() throws Exception {
    // PNG magic number: 89 50 4E 47 0D 0A 1A 0A
    byte[] content = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    InputStream is = new BufferedInputStream(new ByteArrayInputStream(content));

    assertDoesNotThrow(() -> validator.validate("icon.png", is));
  }

  @Test
  void testMagicNumberVerification_PdfSuccess() throws Exception {
    // PDF magic number: %PDF- (25 50 44 46 2D)
    byte[] content = new byte[] {0x25, 0x50, 0x44, 0x46, 0x2D, 0, 0, 0};
    InputStream is = new BufferedInputStream(new ByteArrayInputStream(content));

    assertDoesNotThrow(() -> validator.validate("report.pdf", is));
  }

  @Test
  void testMagicNumberVerification_MismatchFails() throws Exception {
    // Executable content masquerading as JPEG
    byte[] content = new byte[] {0x4D, 0x5A, 0, 0, 0, 0, 0, 0}; // MZ (Windows EXE)
    InputStream is = new BufferedInputStream(new ByteArrayInputStream(content));

    assertThrows(SecurityValidationException.class, () -> validator.validate("fake.jpg", is));
  }

  @Test
  void testMagicNumberVerification_WrongExtensionFails() throws Exception {
    // PDF content with PNG extension
    byte[] content = new byte[] {0x25, 0x50, 0x44, 0x46, 0x2D, 0, 0, 0};
    InputStream is = new BufferedInputStream(new ByteArrayInputStream(content));

    assertThrows(SecurityValidationException.class, () -> validator.validate("not_a_pdf.png", is));
  }
}
