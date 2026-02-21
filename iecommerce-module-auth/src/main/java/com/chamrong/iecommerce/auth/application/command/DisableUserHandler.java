package com.chamrong.iecommerce.auth.application.command;

import com.chamrong.iecommerce.auth.domain.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DisableUserHandler {

  private final UserRepository userRepository;

  public DisableUserHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /** Soft-disable a user account without deleting it. */
  @Transactional
  public void handle(Long id) {
    var user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    user.setEnabled(false);
    userRepository.save(user);
  }
}
