package com.chamrong.iecommerce.auth.application.command.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.command.ChangePasswordCommand;
import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.auth.LoginUserHandler;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.UserAccountState;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/** Unit tests for {@link ChangePasswordHandler}. */
@ExtendWith(MockitoExtension.class)
class ChangePasswordHandlerTest {

  private static final String USERNAME = "user@example.com";
  private static final String TENANT_ID = "TENANT-1";

  @Mock private IdentityService identityService;
  @Mock private LoginUserHandler loginUserHandler;
  @Mock private UserRepositoryPort userRepository;

  private ChangePasswordHandler handler;

  @BeforeEach
  void setUp() {
    handler = new ChangePasswordHandler(identityService, loginUserHandler, userRepository);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    TenantContext.clear();
  }

  @Test
  void handleShouldThrowWhenNoAuthenticatedUser() {
    SecurityContextHolder.clearContext();
    TenantContext.setCurrentTenant(TENANT_ID);

    ChangePasswordCommand cmd = new ChangePasswordCommand("current", "new-password");

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessageContaining("No authenticated user");

    verify(loginUserHandler, never()).handle(any(LoginCommand.class));
    verify(identityService, never()).updatePassword(any(), any());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void handleShouldThrowWhenCurrentPasswordInvalid() {
    setAuthenticatedUser();
    TenantContext.setCurrentTenant(TENANT_ID);

    ChangePasswordCommand cmd = new ChangePasswordCommand("wrong-current", "new-password");

    when(loginUserHandler.handle(any(LoginCommand.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessageContaining("Invalid current password");

    verify(identityService, never()).updatePassword(any(), any());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void handleShouldUpdatePasswordAndActivatePendingUser() {
    setAuthenticatedUser();
    TenantContext.setCurrentTenant(TENANT_ID);

    ChangePasswordCommand cmd = new ChangePasswordCommand("current", "new-secret");

    when(loginUserHandler.handle(any(LoginCommand.class))).thenReturn(null);
    when(identityService.lookupId(USERNAME)).thenReturn("kc-id-1");

    User pending = new User(TENANT_ID, USERNAME, "user@example.com");
    pending.pendingActivation();
    when(userRepository.findByUsernameAndTenantId(USERNAME, TENANT_ID))
        .thenReturn(Optional.of(pending));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    when(userRepository.save(userCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    handler.handle(cmd);

    verify(identityService).updatePassword("kc-id-1", "new-secret");

    User saved = userCaptor.getValue();
    // Password change timestamp set
    Instant changedAt = saved.getLastPasswordChangeAt();
    org.assertj.core.api.Assertions.assertThat(changedAt).isNotNull();
    // Pending account transitioned to ACTIVE
    org.assertj.core.api.Assertions.assertThat(saved.getAccountState())
        .isEqualTo(UserAccountState.ACTIVE);
  }

  private void setAuthenticatedUser() {
    var authentication =
        new UsernamePasswordAuthenticationToken(USERNAME, "N/A", java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
