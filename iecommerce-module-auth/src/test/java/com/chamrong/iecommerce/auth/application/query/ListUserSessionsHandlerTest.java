package com.chamrong.iecommerce.auth.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.UserSession;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListUserSessionsHandlerTest {

  @Mock private IdentityService identityService;

  @InjectMocks private ListUserSessionsHandler handler;

  @Test
  void handleShouldDelegateToIdentityServiceAndReturnSessions() {
    String keycloakId = "kc-id-1";
    List<UserSession> sessions =
        List.of(
            new UserSession("s1", "127.0.0.1", "Chrome", Instant.now(), Instant.now()),
            new UserSession("s2", "127.0.0.2", "Firefox", Instant.now(), Instant.now()));

    when(identityService.listActiveSessions(keycloakId)).thenReturn(sessions);

    List<UserSession> result = handler.handle(keycloakId);

    verify(identityService).listActiveSessions(keycloakId);
    assertThat(result).isEqualTo(sessions);
  }
}
