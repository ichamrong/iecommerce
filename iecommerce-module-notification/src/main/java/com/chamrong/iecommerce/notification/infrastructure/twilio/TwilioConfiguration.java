package com.chamrong.iecommerce.notification.infrastructure.twilio;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "iecommerce.twilio")
public class TwilioConfiguration {

  private String accountSid;
  private String authToken;
  private String systemPhoneNumber;

  @PostConstruct
  public void init() {
    if (accountSid != null && !accountSid.isBlank() && !accountSid.equals("dummy_sid")) {
      Twilio.init(accountSid, authToken);
      log.info("Twilio SDK initialized with active account: {}", accountSid);
    } else {
      log.warn("Twilio SDK not initialized. Using mock/dummy credentials.");
    }
  }
}
