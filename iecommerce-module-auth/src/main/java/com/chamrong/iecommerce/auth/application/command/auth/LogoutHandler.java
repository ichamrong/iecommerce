package com.chamrong.iecommerce.auth.application.command.auth;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Handles logging out a user via the Identity Provider. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutHandler {

  private final IdentityService identityService;

  /**
   * Logs out the user by invalidating their refresh token.
   *
   * @param cmd command containing the active refresh token
   */
  public void handle(LogoutCommand cmd) {
    log.debug("Logging out user via IDP");
    identityService.logout(cmd.refreshToken());
    log.info("User logged out successfully");
  }
}
