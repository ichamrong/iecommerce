package com.chamrong.iecommerce.auth.application.command.user;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.event.UserDisabledEvent;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Soft-disables a user account (local DB + Keycloak) without deleting it. */
@Component
@RequiredArgsConstructor
public class DisableUserHandler {

  private final UserRepositoryPort userRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final IdentityService identityService;

  @Transactional
  public void handle(Long id) {
    var user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    user.disable();
    userRepository.save(user);

    identityService.disableUser(user.getKeycloakId());

    eventPublisher.publishEvent(new UserDisabledEvent(id, user.getTenantId()));
  }
}
