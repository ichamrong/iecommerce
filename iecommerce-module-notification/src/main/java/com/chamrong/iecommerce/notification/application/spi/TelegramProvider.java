package com.chamrong.iecommerce.notification.application.spi;

public interface TelegramProvider {
  void sendMessage(String chatId, String content);
}
