package com.chamrong.iecommerce.invoice.infrastructure.email;

import com.chamrong.iecommerce.invoice.domain.port.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * SMTP adapter for invoice email delivery using Spring's {@link JavaMailSender}.
 *
 * <h2>Email Structure</h2>
 *
 * <ul>
 *   <li>{@code text/html} body — invoice summary + "Verify Invoice" link
 *   <li>{@code application/pdf} attachment — signed invoice PDF
 * </ul>
 *
 * <h2>Security</h2>
 *
 * <ul>
 *   <li>ASVS V7.1.1 — Recipient address is masked in log output.
 *   <li>ASVS V7.1.1 — PDF bytes are never logged (not size, not content).
 *   <li>No SMTP credentials appear in logs — Spring's {@link JavaMailSender} handles auth
 *       internally.
 * </ul>
 *
 * <h2>Feature flag</h2>
 *
 * Only instantiated when {@code invoice.email.enabled=true} (default). Set to {@code false} in test
 * or non-prod environments to suppress real email delivery.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "invoice.email.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JavaMailInvoiceEmailAdapter.InvoiceEmailProperties.class)
public class JavaMailInvoiceEmailAdapter implements EmailSenderPort {

  private final JavaMailSender mailSender;
  private final InvoiceEmailProperties props;

  public JavaMailInvoiceEmailAdapter(JavaMailSender mailSender, InvoiceEmailProperties props) {
    this.mailSender = mailSender;
    this.props = props;
  }

  @Override
  public void sendInvoiceEmail(InvoiceEmailRequest request) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      // multipart = true for attachment; utf-8 for full Unicode support
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(props.fromAddress());
      helper.setTo(request.toAddress());
      helper.setSubject(
          "Invoice " + request.invoiceNumber() + " — Please find your signed invoice");
      helper.setText(buildHtmlBody(request), true);

      // PDF attachment — filename uses invoice number for clarity
      String filename = "invoice-" + request.invoiceNumber() + ".pdf";
      helper.addAttachment(
          filename, () -> new java.io.ByteArrayInputStream(request.pdfBytes()), "application/pdf");

      mailSender.send(message);

      // ASVS V7.1.1 — mask recipient in log; never log pdfBytes size or content
      log.info(
          "Invoice email dispatched: invoiceId={} tenant={} recipient={}",
          request.invoiceId(),
          request.tenantId(),
          maskEmail(request.toAddress()));

    } catch (MessagingException ex) {
      // Sanitize exception message — MessagingException may contain server responses with PII
      String sanitized = "SMTP failure: " + ex.getClass().getSimpleName();
      log.warn(
          "Invoice email failed: invoiceId={} tenant={} reason={}",
          request.invoiceId(),
          request.tenantId(),
          sanitized);
      throw new EmailDeliveryException(sanitized, ex);
    }
  }

  // ── HTML template ─────────────────────────────────────────────────────────

  private String buildHtmlBody(InvoiceEmailRequest req) {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Invoice %s</title>
  <style>
    body { font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
    .header { background: #1a56db; color: white; padding: 20px; border-radius: 6px 6px 0 0; }
    .content { background: #f9fafb; padding: 24px; border: 1px solid #e5e7eb; }
    .btn { display: inline-block; background: #1a56db; color: white; padding: 12px 24px;
           text-decoration: none; border-radius: 6px; font-weight: bold; margin: 16px 0; }
    .sig-box { background: #fff; border: 1px solid #e5e7eb; border-radius: 4px;
               padding: 12px; margin-top: 20px; font-family: monospace; font-size: 11px;
               color: #6b7280; word-break: break-all; }
    .footer { color: #9ca3af; font-size: 11px; margin-top: 24px; text-align: center; }
  </style>
</head>
<body>
  <div class="header"><h2 style="margin:0">Invoice %s</h2></div>
  <div class="content">
    <p>Please find your signed invoice attached as a PDF.</p>
    <p>This invoice has been <strong>digitally signed</strong> using Ed25519 cryptography.
       You can verify its authenticity at any time using the link below or by providing
       the signature metadata to our verification endpoint.</p>
    <a href="%s" class="btn">&#10003; Verify This Invoice</a>
    <p>If the button does not work, copy and paste this link:<br>
       <a href="%s">%s</a></p>
    <div class="sig-box">
      <strong>Invoice Number:</strong> %s<br>
      <strong>Invoice ID:</strong> %s
    </div>
  </div>
  <div class="footer">
    This is an automated message. Do not reply to this email.<br>
    If you have questions, contact your account manager.
  </div>
</body>
</html>
"""
        .formatted(
            req.invoiceNumber(), // title
            req.invoiceNumber(), // header h2
            req.verifyUrl(), // button href
            req.verifyUrl(), // fallback link href
            req.verifyUrl(), // fallback link text
            req.invoiceNumber(), // sig-box invoice number
            req.invoiceId() // sig-box invoice id
            );
  }

  // ── Log safety ─────────────────────────────────────────────────────────────

  /**
   * Masks an email address for log output. e.g. {@code john.doe@example.com} → {@code
   * joh**@example.com}
   */
  static String maskEmail(String email) {
    if (email == null) return "[null]";
    int at = email.indexOf('@');
    if (at < 3) return "**@" + (at >= 0 ? email.substring(at + 1) : "?");
    return email.substring(0, 3) + "**" + email.substring(at);
  }

  // ── Configuration properties ───────────────────────────────────────────────

  /**
   * Configuration for invoice email delivery.
   *
   * <p>Required properties (in {@code application.yml}):
   *
   * <pre>
   * invoice:
   *   email:
   *     enabled: true
   *     from-address: invoices@company.tld
   *     verification-base-url: https://app.company.tld/api/v1
   * </pre>
   */
  @ConfigurationProperties(prefix = "invoice.email")
  public record InvoiceEmailProperties(
      /** SMTP From address, e.g. {@code "Invoices <invoices@company.tld>"}. */
      String fromAddress,
      /** Base URL for the "Verify Invoice" link, without trailing slash. */
      String verificationBaseUrl,
      /** Feature flag — set {@code false} to suppress email sending (non-prod). */
      boolean enabled) {

    public InvoiceEmailProperties {
      if (enabled && (fromAddress == null || fromAddress.isBlank())) {
        throw new IllegalStateException(
            "invoice.email.from-address must be set when invoice.email.enabled=true");
      }
    }
  }
}
