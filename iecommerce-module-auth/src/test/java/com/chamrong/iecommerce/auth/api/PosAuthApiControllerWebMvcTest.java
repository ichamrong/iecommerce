package com.chamrong.iecommerce.auth.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.PosAuthDto;
import com.chamrong.iecommerce.auth.application.PosService;
import com.chamrong.iecommerce.auth.domain.PosSession;
import com.chamrong.iecommerce.auth.domain.PosTerminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

/** Focused tests for {@link PosAuthApiController} using direct method calls. */
@ExtendWith(MockitoExtension.class)
class PosAuthApiControllerWebMvcTest {

  private static final String TENANT_ID = "TENANT-1";

  @Mock private PosService posService;

  private PosAuthApiController controller;

  @BeforeEach
  void setUp() {
    controller = new PosAuthApiController(posService);
  }

  @Test
  void registerTerminalShouldDelegateToService() {
    Jwt jwt =
        Jwt.withTokenValue("token").header("alg", "none").claim("tenant_id", TENANT_ID).build();
    PosAuthDto.TerminalRegisterRequest req =
        new PosAuthDto.TerminalRegisterRequest("POS-1", "HW-1", "BR-1");

    PosTerminal terminal = new PosTerminal(TENANT_ID, "POS-1", "HW-1", "BR-1");
    when(posService.registerTerminal(eq(TENANT_ID), any(), any(), any())).thenReturn(terminal);

    var response = controller.registerTerminal(jwt, req);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(posService).registerTerminal(TENANT_ID, "POS-1", "HW-1", "BR-1");
  }

  @Test
  void listTerminalsShouldDelegateToService() {
    Jwt jwt =
        Jwt.withTokenValue("token").header("alg", "none").claim("tenant_id", TENANT_ID).build();

    when(posService.listTerminals(TENANT_ID))
        .thenReturn(java.util.List.of(new PosTerminal(TENANT_ID, "POS-1", "HW-1", "BR-1")));

    var response = controller.listTerminals(jwt);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(posService).listTerminals(TENANT_ID);
  }

  @Test
  void openSessionShouldDelegateToService() {
    Jwt jwt =
        Jwt.withTokenValue("token").header("alg", "none").claim("tenant_id", TENANT_ID).build();
    PosAuthDto.SessionOpenRequest req = new PosAuthDto.SessionOpenRequest(1L, 10L);

    PosSession session = new PosSession(TENANT_ID, 1L, 10L);
    when(posService.openSession(eq(TENANT_ID), anyLong(), anyLong())).thenReturn(session);

    var response = controller.openSession(jwt, req);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(posService).openSession(TENANT_ID, 1L, 10L);
  }

  @Test
  void closeSessionShouldDelegateToService() {
    PosAuthDto.SessionCloseRequest req = new PosAuthDto.SessionCloseRequest("End of shift");

    var response = controller.closeSession(5L, req);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(posService).closeSession(5L, "End of shift");
  }
}
