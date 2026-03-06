package com.chamrong.iecommerce.auth.application.command.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/** Unit tests for {@link Enable2FAHandler}. */
@ExtendWith(MockitoExtension.class)
class Enable2FAHandlerTest {

  @Mock private IdentityService identityService;

  @InjectMocks private Enable2FAHandler handler;

  @BeforeEach
  void setUp() {
    var authentication =
        new UsernamePasswordAuthenticationToken("user@example.com", "N/A", java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void handleShouldEnableTotpForCurrentUser() {
    when(identityService.lookupId("user@example.com")).thenReturn("kc-id-1");

    handler.handle();

    verify(identityService).lookupId("user@example.com");
    verify(identityService).enableTotpForUser("kc-id-1");
  }
}
