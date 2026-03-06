package com.chamrong.iecommerce.auth.application.command.auth;

import static org.mockito.Mockito.verify;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutHandlerTest {

  @Mock private IdentityService identityService;

  @InjectMocks private LogoutHandler handler;

  @Test
  void handleShouldDelegateToIdentityService() {
    var cmd = new LogoutCommand("refresh-token");

    handler.handle(cmd);

    verify(identityService).logout("refresh-token");
  }
}
