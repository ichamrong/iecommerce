package com.chamrong.iecommerce.auth.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chamrong.iecommerce.auth.application.command.ChangeCredentialsCommand;
import com.chamrong.iecommerce.auth.application.command.ChangePasswordCommand;
import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.command.auth.LoginUserHandler;
import com.chamrong.iecommerce.auth.application.command.auth.LogoutCommand;
import com.chamrong.iecommerce.auth.application.command.auth.LogoutHandler;
import com.chamrong.iecommerce.auth.application.command.auth.RefreshTokenCommand;
import com.chamrong.iecommerce.auth.application.command.auth.RefreshTokenHandler;
import com.chamrong.iecommerce.auth.application.command.password.ForgotPasswordHandler;
import com.chamrong.iecommerce.auth.application.command.security.ChangeCredentialsHandler;
import com.chamrong.iecommerce.auth.application.command.security.ChangePasswordHandler;
import com.chamrong.iecommerce.auth.application.command.user.RegisterUserHandler;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.application.query.GetMeHandler;
import com.chamrong.iecommerce.auth.application.query.ListSocialProvidersQueryHandler;
import com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType;
import com.chamrong.iecommerce.auth.domain.idp.SocialProvider;
import com.chamrong.iecommerce.auth.infrastructure.config.AuthCookieHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Thin MVC tests for {@link AuthController} using standalone MockMvc setup.
 *
 * <p>Handlers are mocked at the controller boundary so these tests stay fast and focused on
 * request/response contracts.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerWebMvcTest {

  private MockMvc mockMvc;

  @Mock private RegisterUserHandler registerUserHandler;
  @Mock private LoginUserHandler loginUserHandler;
  @Mock private ForgotPasswordHandler forgotPasswordHandler;
  @Mock private ChangePasswordHandler changePasswordHandler;
  @Mock private ChangeCredentialsHandler changeCredentialsHandler;
  @Mock private RefreshTokenHandler refreshTokenHandler;
  @Mock private LogoutHandler logoutHandler;
  @Mock private ListSocialProvidersQueryHandler listSocialProvidersQueryHandler;
  @Mock private AuthCookieHelper authCookieHelper;
  @Mock private GetMeHandler getMeHandler;

  @BeforeEach
  void setUp() {
    AuthController controller =
        new AuthController(
            registerUserHandler,
            loginUserHandler,
            forgotPasswordHandler,
            changePasswordHandler,
            changeCredentialsHandler,
            refreshTokenHandler,
            logoutHandler,
            listSocialProvidersQueryHandler,
            authCookieHelper,
            getMeHandler);
    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void loginEndpointShouldReturnAuthResponseFromHandler() throws Exception {
    AuthResponse authResponse =
        new AuthResponse("access-token", "refresh-token", 300, "Bearer", "session-1");
    when(loginUserHandler.handle(any(LoginCommand.class))).thenReturn(authResponse);

    String body =
        """
        {
          "username": "user@example.com",
          "password": "secret",
          "tenantId": "TENANT"
        }
        """;

    mockMvc
        .perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").value("access-token"))
        .andExpect(jsonPath("$.refresh_token").value("refresh-token"));
  }

  @Test
  void forgotPasswordEndpointShouldAlwaysReturnOk() throws Exception {
    String body =
        """
        {
          "email": "user@example.com",
          "tenantId": "TENANT"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk());

    verify(forgotPasswordHandler).handle(any());
  }

  @Test
  void registerEndpointShouldReturnCreatedWithAuthResponse() throws Exception {
    AuthResponse authResponse =
        new AuthResponse("access-token", "refresh-token", 300, "Bearer", "session-1");
    when(registerUserHandler.handle(any(RegisterCommand.class))).thenReturn(authResponse);

    String body =
        """
        {
          "username": "user@example.com",
          "email": "user@example.com",
          "password": "secret",
          "tenantId": "TENANT"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.access_token").value("access-token"))
        .andExpect(jsonPath("$.refresh_token").value("refresh-token"));
  }

  @Test
  void refreshTokenEndpointShouldReturnAuthResponse() throws Exception {
    AuthResponse authResponse =
        new AuthResponse("access-token", "refresh-token", 300, "Bearer", "session-1");
    when(refreshTokenHandler.handle(any(RefreshTokenCommand.class))).thenReturn(authResponse);

    String body =
        """
        {
          "refreshToken": "refresh-token"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").value("access-token"))
        .andExpect(jsonPath("$.refresh_token").value("refresh-token"));
  }

  @Test
  void logoutEndpointShouldReturnNoContent() throws Exception {
    String body =
        """
        {
          "refreshToken": "refresh-token"
        }
        """;

    mockMvc
        .perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isNoContent());

    verify(logoutHandler).handle(any(LogoutCommand.class));
  }

  @Test
  void changePasswordEndpointShouldDelegateToHandler() throws Exception {
    String body =
        """
        {
          "currentPassword": "current",
          "newPassword": "new-secret"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNoContent());

    verify(changePasswordHandler).handle(any(ChangePasswordCommand.class));
  }

  @Test
  void changeCredentialsEndpointShouldDelegateToHandler() throws Exception {
    String body =
        """
        {
          "currentPassword": "current",
          "newUsername": "new-user",
          "newPassword": "new-secret"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/change-credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNoContent());

    verify(changeCredentialsHandler).handle(any(ChangeCredentialsCommand.class));
  }

  @Test
  void socialProvidersEndpointShouldReturnProvidersList() throws Exception {
    when(listSocialProvidersQueryHandler.handle())
        .thenReturn(
            java.util.List.of(
                new SocialProvider(IdentityProviderType.GOOGLE, "google", "Google", true)));

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                    "/api/v1/auth/social-providers")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].alias").value("google"))
        .andExpect(jsonPath("$[0].enabled").value(true));
  }
}
