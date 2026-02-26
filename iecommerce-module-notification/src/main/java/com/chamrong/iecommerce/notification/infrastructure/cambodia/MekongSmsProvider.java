package com.chamrong.iecommerce.notification.infrastructure.cambodia;

import com.chamrong.iecommerce.notification.application.spi.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MekongSmsProvider implements SmsProvider {

  @Override
  public void sendSms(String recipient, String content) {
    log.info("Dispatching SMS via MekongSMS to {}: {}", recipient, content);
    // TODO: Implement actual MekongSMS API call
  }

  @Override
  public boolean supports(String providerName) {
    return "mekong".equalsIgnoreCase(providerName);
  }
}
