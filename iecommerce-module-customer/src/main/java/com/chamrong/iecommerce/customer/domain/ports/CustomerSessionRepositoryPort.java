package com.chamrong.iecommerce.customer.domain.ports;

/**
 * Port for customer session storage (e.g. Redis). Used for session revocation after password reset.
 */
public interface CustomerSessionRepositoryPort {

  void registerSession(String customerId, String sessionId, String deviceMetadata);

  void invalidateOtherSessions(String customerId, String currentSessionId);

  void invalidateAll(String customerId);

  /** Count active sessions for the customer (for max-sessions policy). */
  int countActiveSessions(String customerId);
}
