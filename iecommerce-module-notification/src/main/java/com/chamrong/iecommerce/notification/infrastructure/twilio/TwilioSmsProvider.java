package com.chamrong.iecommerce.notification.infrastructure.twilio;

import com.chamrong.iecommerce.notification.application.spi.SmsProvider;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwilioSmsProvider implements SmsProvider {

  private final TwilioConfiguration config;

  @Override
  public void sendSms(String recipient, String content) {
    if ("dummy_sid".equals(config.getAccountSid())) {
      log.info("[MOCK] Twilio sending SMS to {}: {}", recipient, content);
      return;
    }

    try {
      Message.creator(
              new PhoneNumber(recipient), new PhoneNumber(config.getSystemPhoneNumber()), content)
          .create();
      log.info("Twilio SMS sent to {}", recipient);
    } catch (Exception e) {
      log.error("Twilio SMS failed for {}: {}", recipient, e.getMessage());
      throw new RuntimeException("Twilio dispatch failed", e);
    }
  }

  @Override
  public boolean supports(String providerName) {
    return "twilio".equalsIgnoreCase(providerName);
  }
}
