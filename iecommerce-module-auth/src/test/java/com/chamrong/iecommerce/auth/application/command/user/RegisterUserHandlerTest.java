package com.chamrong.iecommerce.auth.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.application.exception.InvalidPasswordException;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.event.UserRegisteredEvent;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import com.chamrong.iecommerce.auth.testsupport.AuthTestDataFactory;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Unit tests for {@link RegisterUserHandler}. */
@ExtendWith(MockitoExtension.class)
class RegisterUserHandlerTest {

  private static final String TENANT_ID = "TENANT-1";

  @Mock private UserRepositoryPort userRepository;
  @Mock private RoleRepositoryPort roleRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private IdentityService identityService;

  @Mock
  private com.chamrong.iecommerce.auth.application.command.auth.LoginUserHandler loginUserHandler;

  @InjectMocks private RegisterUserHandler handler;

  @Test
  void handleShouldThrowWhenUsernameAlreadyExists() {
    RegisterCommand cmd =
        new RegisterCommand("alice", "alice@example.com", "Secret123!", TENANT_ID);

    when(userRepository.findByUsernameAndTenantId("alice", TENANT_ID))
        .thenReturn(Optional.of(AuthTestDataFactory.user("alice")));

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(DuplicateUserException.class)
        .hasMessageContaining("Username already exists");

    verify(identityService, never()).registerUser(any(RegisterCommand.class));
    verify(loginUserHandler, never()).handle(any(LoginCommand.class));
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void handleShouldThrowWhenEmailAlreadyExists() {
    RegisterCommand cmd =
        new RegisterCommand("alice", "alice@example.com", "Secret123!", TENANT_ID);

    when(userRepository.findByUsernameAndTenantId("alice", TENANT_ID)).thenReturn(Optional.empty());
    when(userRepository.findByEmailAndTenantId("alice@example.com", TENANT_ID))
        .thenReturn(Optional.of(AuthTestDataFactory.user("alice")));

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(DuplicateUserException.class)
        .hasMessageContaining("Email already exists");

    verify(identityService, never()).registerUser(any(RegisterCommand.class));
    verify(loginUserHandler, never()).handle(any(LoginCommand.class));
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void handleShouldRejectTooShortPassword() {
    RegisterCommand cmd = new RegisterCommand("alice", "alice@example.com", "short", TENANT_ID);

    when(userRepository.findByUsernameAndTenantId("alice", TENANT_ID)).thenReturn(Optional.empty());
    when(userRepository.findByEmailAndTenantId("alice@example.com", TENANT_ID))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(InvalidPasswordException.class)
        .hasMessageContaining("Password must be at least 8 characters long");

    verify(identityService, never()).registerUser(any(RegisterCommand.class));
    verify(loginUserHandler, never()).handle(any(LoginCommand.class));
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void handleShouldRegisterUserAndReturnAuthResponse() {
    RegisterCommand cmd =
        new RegisterCommand("alice", "alice@example.com", "Secret123!", TENANT_ID);

    when(userRepository.findByUsernameAndTenantId("alice", TENANT_ID)).thenReturn(Optional.empty());
    when(userRepository.findByEmailAndTenantId("alice@example.com", TENANT_ID))
        .thenReturn(Optional.empty());

    when(identityService.registerUser(cmd)).thenReturn("kc-id-1");

    Role role = new Role(Role.ROLE_CUSTOMER);
    when(roleRepository.findByName(Role.ROLE_CUSTOMER)).thenReturn(Optional.of(role));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    when(userRepository.save(userCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AuthResponse authResponse = new AuthResponse("access", "refresh", 3600, "Bearer", "session-id");
    when(loginUserHandler.handle(
            eq(new LoginCommand(cmd.username(), cmd.password(), cmd.tenantId()))))
        .thenReturn(authResponse);

    ArgumentCaptor<UserRegisteredEvent> eventCaptor =
        ArgumentCaptor.forClass(UserRegisteredEvent.class);

    AuthResponse result = handler.handle(cmd);

    assertThat(result).isSameAs(authResponse);

    verify(identityService).registerUser(cmd);
    verify(roleRepository).findByName(Role.ROLE_CUSTOMER);
    verify(userRepository).save(any(User.class));

    User saved = userCaptor.getValue();
    assertThat(saved.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(saved.getUsername()).isEqualTo("alice");
    assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    assertThat(saved.getRoles()).contains(role);

    verify(eventPublisher).publishEvent(eventCaptor.capture());
    UserRegisteredEvent event = eventCaptor.getValue();
    assertThat(event.username()).isEqualTo("alice");
    assertThat(event.email()).isEqualTo("alice@example.com");
    assertThat(event.tenantId()).isEqualTo(TENANT_ID);
  }
}
