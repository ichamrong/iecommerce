package com.chamrong.iecommerce.auth.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chamrong.iecommerce.auth.application.command.security.Disable2FAHandler;
import com.chamrong.iecommerce.auth.application.command.security.Enable2FAHandler;
import com.chamrong.iecommerce.auth.application.command.security.ResetUserPasswordHandler;
import com.chamrong.iecommerce.auth.application.command.security.RevokeAllSessionsHandler;
import com.chamrong.iecommerce.auth.application.command.security.RevokeSessionHandler;
import com.chamrong.iecommerce.auth.application.command.security.TriggerEmailVerificationHandler;
import com.chamrong.iecommerce.auth.application.command.security.UnlockUserHandler;
import com.chamrong.iecommerce.auth.application.command.user.AdminCreateUserCommand;
import com.chamrong.iecommerce.auth.application.command.user.AdminCreateUserHandler;
import com.chamrong.iecommerce.auth.application.command.user.DisableUserHandler;
import com.chamrong.iecommerce.auth.application.query.UserQueryHandler;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Standalone MVC tests for {@link UserController} using mocked handlers. */
@ExtendWith(MockitoExtension.class)
class UserControllerWebMvcTest {

  private static final String TENANT_ID = "TENANT-1";

  private MockMvc mockMvc;

  @Mock private UserQueryHandler userQueryHandler;
  @Mock private DisableUserHandler disableUserHandler;
  @Mock private AdminCreateUserHandler adminCreateUserHandler;
  @Mock private Enable2FAHandler enable2FAHandler;
  @Mock private Disable2FAHandler disable2FAHandler;
  @Mock private UnlockUserHandler unlockUserHandler;
  @Mock private TriggerEmailVerificationHandler triggerEmailVerificationHandler;
  @Mock private RevokeSessionHandler revokeSessionHandler;
  @Mock private RevokeAllSessionsHandler revokeAllSessionsHandler;
  @Mock private ResetUserPasswordHandler resetUserPasswordHandler;

  @BeforeEach
  void setUp() {
    UserController controller =
        new UserController(
            userQueryHandler,
            disableUserHandler,
            adminCreateUserHandler,
            enable2FAHandler,
            disable2FAHandler,
            unlockUserHandler,
            triggerEmailVerificationHandler,
            revokeSessionHandler,
            revokeAllSessionsHandler,
            resetUserPasswordHandler);
    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void listUsersShouldClampLimitAndDelegateToHandler() throws Exception {
    TenantContext.setCurrentTenant(TENANT_ID);

    when(userQueryHandler.listUsers(anyString(), any(), anyInt(), any())).thenReturn(null);

    mockMvc.perform(get("/api/v1/users").param("limit", "500")).andExpect(status().isOk());

    ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(userQueryHandler).listUsers(anyString(), any(), limitCaptor.capture(), any());

    // limit=500 should be clamped to at most 100
    org.assertj.core.api.Assertions.assertThat(limitCaptor.getValue()).isEqualTo(100);
  }

  @Test
  void getUserShouldReturnOkWhenUserFound() throws Exception {
    TenantContext.setCurrentTenant(TENANT_ID);
    User user = new User(TENANT_ID, "alice", "alice@example.com");
    when(userQueryHandler.findUserById(1L)).thenReturn(Optional.of(user));

    mockMvc.perform(get("/api/v1/users/{id}", 1L)).andExpect(status().isOk());
  }

  @Test
  void getUserShouldReturnNotFoundWhenMissing() throws Exception {
    TenantContext.setCurrentTenant(TENANT_ID);
    when(userQueryHandler.findUserById(1L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/v1/users/{id}", 1L)).andExpect(status().isNotFound());
  }

  @Test
  void adminCreateUserShouldReturnCreated() throws Exception {
    String body =
        """
        {
          "username": "admin-created",
          "email": "admin@example.com",
          "temporaryPassword": "Temp1234!",
          "tenantId": "TENANT-1"
        }
        """;

    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());

    verify(adminCreateUserHandler).handle(any(AdminCreateUserCommand.class));
  }

  @Test
  void disableUserShouldReturnNoContent() throws Exception {
    mockMvc.perform(patch("/api/v1/users/{id}/disable", 99L)).andExpect(status().isNoContent());

    verify(disableUserHandler).handle(99L);
  }

  @Test
  void enableAndDisable2FaEndpointsShouldDelegateToHandlers() throws Exception {
    mockMvc.perform(post("/api/v1/users/me/2fa")).andExpect(status().isOk());

    verify(enable2FAHandler).handle();

    mockMvc.perform(delete("/api/v1/users/me/2fa")).andExpect(status().isNoContent());

    verify(disable2FAHandler).handle();
  }

  @Test
  void unlockUserShouldDelegateToHandler() throws Exception {
    TenantContext.setCurrentTenant(TENANT_ID);

    mockMvc.perform(post("/api/v1/users/{username}/unlock", "alice")).andExpect(status().isOk());

    verify(unlockUserHandler).handle(anyString(), anyString());
  }

  @Test
  void triggerVerificationEmailShouldDelegateToHandler() throws Exception {
    mockMvc
        .perform(post("/api/v1/users/{username}/verify-email", "alice"))
        .andExpect(status().isOk());

    verify(triggerEmailVerificationHandler).handle("alice");
  }

  @Test
  void revokeSessionEndpointsShouldDelegateToHandlers() throws Exception {
    String body = "\"kc-id-1\"";

    mockMvc
        .perform(
            delete("/api/v1/users/sessions/{sessionId}", "sess-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNoContent());

    verify(revokeSessionHandler).handle(anyString(), anyString());

    mockMvc
        .perform(
            delete("/api/v1/users/{username}/sessions", "alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNoContent());

    verify(revokeAllSessionsHandler).handle(anyString());
  }

  @Test
  void resetPasswordShouldDelegateToHandler() throws Exception {
    String body = "\"Temp1234!\"";

    mockMvc
        .perform(
            post("/api/v1/users/{username}/reset-password", "alice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());

    verify(resetUserPasswordHandler).handle(anyString(), anyString());
  }
}
