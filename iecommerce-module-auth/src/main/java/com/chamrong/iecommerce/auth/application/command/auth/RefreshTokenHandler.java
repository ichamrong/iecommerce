package com.chamrong.iecommerce.auth.application.command.auth;

import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Handles exchanging a valid refresh token for a new access token and refresh token pair. */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenHandler {

  private final IdentityService identityService;

  /**
   * Refreshes the token via the Identity Provider.
   *
   * @param cmd command containing the active refresh token
   * @return standard IDP token response
   */
  public AuthResponse handle(RefreshTokenCommand cmd) {
    log.debug("Refreshing token via IDP");
    var response = identityService.refreshToken(cmd.refreshToken());
    log.info("Token refreshed successfully");
    return response;
  }
}
