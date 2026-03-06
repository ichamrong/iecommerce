package com.chamrong.iecommerce.auth.application.command.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.command.ChangeCredentialsCommand;
import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.auth.LoginUserHandler;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.common.TenantContext;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ChangeCredentialsHandlerTest {

  @Mock private IdentityService identityService;
  @Mock private LoginUserHandler loginUserHandler;
  @Mock private UserRepositoryPort userRepository;

  @InjectMocks private ChangeCredentialsHandler handler;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    TenantContext.clear();
  }

  @Test
  void handle_throwsWhenNoAuthentication() {
    TenantContext.setCurrentTenant("TENANT");
    var cmd = new ChangeCredentialsCommand("current", "newuser", "newpassword");

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessageContaining("No authenticated user");
  }

  @Test
  void handle_updatesUsernameAndPasswordAndActivatesPendingUser() {
    var auth = new TestingAuthenticationToken("olduser", "N/A");
    auth.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(auth);
    TenantContext.setCurrentTenant("TENANT");

    var cmd = new ChangeCredentialsCommand("current", "newuser", "newpassword");

    // current password check passes
    when(loginUserHandler.handle(any(LoginCommand.class))).thenReturn(null);

    var user = new User("TENANT", "olduser", "old@example.com");
    user.pendingActivation();

    when(userRepository.findByUsernameAndTenantId("olduser", "TENANT"))
        .thenReturn(Optional.of(user));
    when(userRepository.findByUsernameAndTenantId("newuser", "TENANT"))
        .thenReturn(Optional.empty());
    when(identityService.lookupId("olduser")).thenReturn("keycloak-id");

    handler.handle(cmd);

    verify(identityService).updateUsername("keycloak-id", "newuser");
    verify(identityService).updatePassword("keycloak-id", "newpassword");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void handle_throwsWhenNewUsernameAlreadyExists() {
    var auth = new TestingAuthenticationToken("olduser", "N/A");
    auth.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(auth);
    TenantContext.setCurrentTenant("TENANT");

    var cmd = new ChangeCredentialsCommand("current", "existing", "newpassword");

    when(loginUserHandler.handle(any(LoginCommand.class))).thenReturn(null);

    var existing = new User("TENANT", "existing", "e@example.com");
    when(userRepository.findByUsernameAndTenantId("olduser", "TENANT"))
        .thenReturn(Optional.of(new User("TENANT", "olduser", "old@example.com")));
    when(userRepository.findByUsernameAndTenantId("existing", "TENANT"))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> handler.handle(cmd)).isInstanceOf(DuplicateUserException.class);
  }
}
