package com.chamrong.iecommerce.auth.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.exception.AccountLockedException;
import com.chamrong.iecommerce.auth.application.exception.DuplicateUserException;
import com.chamrong.iecommerce.auth.application.exception.RateLimitExceededException;
import com.chamrong.iecommerce.auth.domain.exception.AuthErrorCode;
import com.chamrong.iecommerce.auth.domain.exception.AuthException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

/** Focused unit tests for {@link AuthExceptionHandler}. */
class AuthExceptionHandlerTest {

  private final AuthExceptionHandler handler = new AuthExceptionHandler();

  @Test
  void handleAuthExceptionShouldMapToErrorCodeStatus() {
    AuthException ex =
        new AuthException(AuthErrorCode.INVALID_CREDENTIALS, "Invalid credentials provided");

    ResponseEntity<Map<String, Object>> response = handler.handleAuthException(ex);

    assertThat(response.getStatusCode()).isEqualTo(AuthErrorCode.INVALID_CREDENTIALS.getStatus());
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("code"))
        .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS.getCode());
  }

  @Test
  void handleBadCredentialsShouldReturnUnauthorized() {
    BadCredentialsException ex = new BadCredentialsException("Bad credentials");

    ResponseEntity<Map<String, Object>> response = handler.handleBadCredentials(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("code")).isEqualTo("AUTH-002");
  }

  @Test
  void handleRateLimitShouldReturnTooManyRequests() {
    RateLimitExceededException ex = new RateLimitExceededException("Too many attempts");

    ResponseEntity<Map<String, Object>> response = handler.handleRateLimit(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("code")).isEqualTo("AUTH-003");
  }

  @Test
  void handleAccountLockedShouldReturnLocked() {
    AccountLockedException ex = new AccountLockedException("user", Duration.ofMinutes(5));

    ResponseEntity<Map<String, Object>> response = handler.handleAccountLocked(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("code")).isEqualTo("AUTH-004");
  }

  @Test
  void handleDuplicateShouldReturnConflict() {
    DuplicateUserException ex = new DuplicateUserException("duplicate");

    ResponseEntity<Map<String, Object>> response = handler.handleDuplicate(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("code")).isEqualTo("AUTH-005");
  }

  @Test
  void handleValidationShouldReturnBadRequestWithFields() {
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getFieldErrors()).thenReturn(List.of());
    when(ex.getBindingResult()).thenReturn(bindingResult);

    ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("code")).isEqualTo("AUTH-006");
    assertThat(response.getBody().get("fields")).isInstanceOf(Map.class);
  }
}
