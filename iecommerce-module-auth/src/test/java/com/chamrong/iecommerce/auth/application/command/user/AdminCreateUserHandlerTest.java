package com.chamrong.iecommerce.auth.application.command.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.Role;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.UserAccountState;
import com.chamrong.iecommerce.auth.domain.event.UserRegisteredEvent;
import com.chamrong.iecommerce.auth.domain.ports.RoleRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Unit tests for {@link AdminCreateUserHandler}. */
@ExtendWith(MockitoExtension.class)
class AdminCreateUserHandlerTest {

  private static final String TENANT_ID = "TENANT-1";

  @Mock private UserRepositoryPort userRepository;
  @Mock private RoleRepositoryPort roleRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private IdentityService identityService;

  @InjectMocks private AdminCreateUserHandler handler;

  @Test
  void handleShouldThrowWhenUsernameAlreadyExists() {
    AdminCreateUserCommand cmd =
        new AdminCreateUserCommand("alice", "alice@example.com", "TempPass123!", TENANT_ID, null);

    when(userRepository.findByUsernameAndTenantId("alice", TENANT_ID))
        .thenReturn(Optional.of(new User(TENANT_ID, "alice", "alice@example.com")));

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(DuplicateUserException.class)
        .hasMessageContaining("Username already exists");

    verify(identityService, never()).createUserWithTemporaryPassword(any(RegisterCommand.class));
    verify(identityService, never()).sendPasswordResetEmail(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void handleShouldThrowWhenEmailAlreadyExists() {
    AdminCreateUserCommand cmd =
        new AdminCreateUserCommand("alice", "alice@example.com", "TempPass123!", TENANT_ID, null);

    when(userRepository.findByUsernameAndTenantId("alice", TENANT_ID)).thenReturn(Optional.empty());
    when(userRepository.findByEmailAndTenantId("alice@example.com", TENANT_ID))
        .thenReturn(Optional.of(new User(TENANT_ID, "alice", "alice@example.com")));

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(DuplicateUserException.class)
        .hasMessageContaining("Email already exists");

    verify(identityService, never()).createUserWithTemporaryPassword(any(RegisterCommand.class));
    verify(identityService, never()).sendPasswordResetEmail(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void handleShouldCreateUserInIdpAndMirrorToLocalDb() {
    AdminCreateUserCommand cmd =
        new AdminCreateUserCommand("alice", "alice@example.com", "TempPass123!", TENANT_ID, null);

    when(userRepository.findByUsernameAndTenantId("alice", TENANT_ID)).thenReturn(Optional.empty());
    when(userRepository.findByEmailAndTenantId("alice@example.com", TENANT_ID))
        .thenReturn(Optional.empty());

    when(identityService.createUserWithTemporaryPassword(any(RegisterCommand.class)))
        .thenReturn("kc-id-1");

    Role tenantRole = new Role(Role.ROLE_CUSTOMER);
    when(roleRepository.findByName(Role.ROLE_CUSTOMER)).thenReturn(Optional.of(tenantRole));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    when(userRepository.save(userCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ArgumentCaptor<UserRegisteredEvent> eventCaptor =
        ArgumentCaptor.forClass(UserRegisteredEvent.class);

    handler.handle(cmd);

    // IDP interactions
    verify(identityService)
        .createUserWithTemporaryPassword(
            new RegisterCommand(
                cmd.username(), cmd.email(), cmd.temporaryPassword(), cmd.tenantId(), null));
    verify(identityService).sendPasswordResetEmail("kc-id-1");

    // Local user persisted with expected state and role
    User saved = userCaptor.getValue();
    org.assertj.core.api.Assertions.assertThat(saved.getTenantId()).isEqualTo(TENANT_ID);
    org.assertj.core.api.Assertions.assertThat(saved.getUsername()).isEqualTo("alice");
    org.assertj.core.api.Assertions.assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    org.assertj.core.api.Assertions.assertThat(saved.getAccountState())
        .isEqualTo(UserAccountState.PENDING);
    org.assertj.core.api.Assertions.assertThat(saved.getRoles()).contains(tenantRole);

    // UserRegisteredEvent published
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    UserRegisteredEvent event = eventCaptor.getValue();
    org.assertj.core.api.Assertions.assertThat(event.username()).isEqualTo("alice");
    org.assertj.core.api.Assertions.assertThat(event.email()).isEqualTo("alice@example.com");
    org.assertj.core.api.Assertions.assertThat(event.tenantId()).isEqualTo(TENANT_ID);
  }
}
