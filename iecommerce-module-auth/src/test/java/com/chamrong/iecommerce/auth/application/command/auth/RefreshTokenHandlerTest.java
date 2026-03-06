package com.chamrong.iecommerce.auth.application.command.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenHandlerTest {

  @Mock private IdentityService identityService;

  @InjectMocks private RefreshTokenHandler handler;

  @Test
  void handleShouldReturnRefreshedTokensFromIdentityService() {
    var cmd = new RefreshTokenCommand("refresh-token");
    var response = new AuthResponse("access", "refresh2", 300, "Bearer", "session-1");

    when(identityService.refreshToken("refresh-token")).thenReturn(response);

    AuthResponse result = handler.handle(cmd);

    verify(identityService).refreshToken("refresh-token");
    assertThat(result).isSameAs(response);
  }
}
