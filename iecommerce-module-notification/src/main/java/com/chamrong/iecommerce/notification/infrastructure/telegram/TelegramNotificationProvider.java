package com.chamrong.iecommerce.notification.infrastructure.telegram;

import com.chamrong.iecommerce.notification.application.spi.TelegramProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TelegramNotificationProvider implements TelegramProvider {

  @Override
  public void sendMessage(String chatId, String content) {
    log.info("Dispatching Telegram message to {}: {}", chatId, content);
    // TODO: Implement actual Telegram Bot API call
  }
}
