package com.chamrong.iecommerce.auth.application.command.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.event.UserDisabledEvent;
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

/** Unit tests for {@link DisableUserHandler}. */
@ExtendWith(MockitoExtension.class)
class DisableUserHandlerTest {

  @Mock private UserRepositoryPort userRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private IdentityService identityService;

  @InjectMocks private DisableUserHandler handler;

  @Test
  void handleShouldThrowWhenUserNotFound() {
    when(userRepository.findById(42L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.handle(42L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("User not found");

    verify(identityService, never()).disableUser(org.mockito.ArgumentMatchers.anyString());
    verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void handleShouldDisableUserAndPublishEvent() {
    User user = AuthTestDataFactory.user("alice");
    user.linkKeycloak("kc-id-1");
    // repository will assign an ID; use reflection-free shortcut by setting via capture

    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    when(userRepository.save(userCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ArgumentCaptor<UserDisabledEvent> eventCaptor =
        ArgumentCaptor.forClass(UserDisabledEvent.class);

    handler.handle(100L);

    User saved = userCaptor.getValue();
    org.assertj.core.api.Assertions.assertThat(saved.isEnabled()).isFalse();

    verify(identityService).disableUser("kc-id-1");

    verify(eventPublisher).publishEvent(eventCaptor.capture());
    UserDisabledEvent event = eventCaptor.getValue();
    org.assertj.core.api.Assertions.assertThat(event.userId()).isEqualTo(100L);
  }
}
