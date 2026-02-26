package com.chamrong.iecommerce.notification.application.spi;

public interface SmsProvider {
  void sendSms(String recipient, String content);

  boolean supports(String providerName);
}
