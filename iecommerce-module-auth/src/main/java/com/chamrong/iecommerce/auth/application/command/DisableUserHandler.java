package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.UserDisabledEvent;
import com.chamrong.iecommerce.auth.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DisableUserHandler {

  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  /** Soft-disable a user account without deleting it. */
  @Transactional
  public void handle(Long id) {
    var user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    user.setEnabled(false);
    userRepository.save(user);
    eventPublisher.publishEvent(new UserDisabledEvent(id, user.getTenantId()));
  }
}
