package com.chamrong.iecommerce.customer.domain.ports;

/** Port for sending email (e.g. password reset link). No token or PII in logs. */
public interface EmailPort {

  /**
   * Send email to the given address. Subject and body must not log the actual token/link in full.
   *
   * @param to recipient email
   * @param subject subject line
   * @param bodyHtml HTML body (e.g. reset link with token)
   */
  void send(String to, String subject, String bodyHtml);
}
