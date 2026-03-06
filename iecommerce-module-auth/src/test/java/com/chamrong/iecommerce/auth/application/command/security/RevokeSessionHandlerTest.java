package com.chamrong.iecommerce.auth.application.command.security;

import static org.mockito.Mockito.verify;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link RevokeSessionHandler}. */
@ExtendWith(MockitoExtension.class)
class RevokeSessionHandlerTest {

  @Mock private IdentityService identityService;

  @InjectMocks private RevokeSessionHandler handler;

  @Test
  void handleShouldRevokeSessionViaIdentityService() {
    String sessionId = "session-123";
    String keycloakId = "kc-id-1";

    handler.handle(sessionId, keycloakId);

    verify(identityService).revokeSession(sessionId);
  }
}
