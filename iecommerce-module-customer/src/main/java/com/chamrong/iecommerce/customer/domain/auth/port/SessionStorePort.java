package com.chamrong.iecommerce.customer.domain.auth.port;

public interface SessionStorePort {
  void registerSession(String customerId, String sessionId, String deviceMetadata);

  void invalidateOtherSessions(String customerId, String currentSessionId);

  void invalidateAll(String customerId);
}
