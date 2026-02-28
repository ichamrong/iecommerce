package com.chamrong.iecommerce.customer.domain.auth.port;

import com.chamrong.iecommerce.customer.application.dto.AuthTokens;

public interface CustomerCredentialPort {
  boolean verify(String customerId, String password);

  AuthTokens generateTokens(String customerId, long tokenVersion, String sessionId);
}
