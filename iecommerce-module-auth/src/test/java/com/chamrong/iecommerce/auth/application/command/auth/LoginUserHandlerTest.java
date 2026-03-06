package com.chamrong.iecommerce.auth.application.command.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.audit.AuthEventLogger;
import com.chamrong.iecommerce.auth.application.command.LoginCommand;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.auth.application.exception.AccountLockedException;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.UserSession;
import com.chamrong.iecommerce.auth.domain.event.ConcurrentSessionDetectedEvent;
import com.chamrong.iecommerce.auth.domain.event.UserLoggedInEvent;
import com.chamrong.iecommerce.auth.domain.event.UserLoginFailedEvent;
import com.chamrong.iecommerce.auth.domain.lock.LoginAttemptRecord;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockPolicy;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class LoginUserHandlerTest {

  private static final String USERNAME = "user@example.com";
  private static final String TENANT_ID = "tenant-1";

  @Mock private IdentityService identityService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private LoginLockStore lockStore;
  @Mock private LoginLockPolicy lockPolicy;
  @Mock private AuthEventLogger auditLog;

  private LoginUserHandler handler;

  @BeforeEach
  void setUp() {
    handler =
        new LoginUserHandler(identityService, eventPublisher, lockStore, lockPolicy, auditLog);
  }

  @Test
  void handleShouldRejectWhenAccountCurrentlyLocked() {
    LoginCommand cmd = new LoginCommand(USERNAME, "secret", TENANT_ID);
    LoginAttemptRecord lockedRecord =
        new LoginAttemptRecord(
            USERNAME, TENANT_ID, 5, Instant.now().plusSeconds(60), Instant.now());

    when(lockStore.find(USERNAME, TENANT_ID)).thenReturn(Optional.of(lockedRecord));

    assertThatThrownBy(() -> handler.handle(cmd)).isInstanceOf(AccountLockedException.class);

    verify(identityService, never()).authenticate(any());
    verify(auditLog).loginRejectedLocked(eq(USERNAME), eq(TENANT_ID), any(Duration.class));
  }

  @Test
  void handleShouldRecordFailureAndApplyLockOnBadCredentials() {
    LoginCommand cmd = new LoginCommand(USERNAME, "wrong", TENANT_ID);
    LoginAttemptRecord current = LoginAttemptRecord.clean(USERNAME, TENANT_ID);

    when(lockStore.find(USERNAME, TENANT_ID)).thenReturn(Optional.of(current));
    when(identityService.authenticate(cmd)).thenThrow(new BadCredentialsException("Bad creds"));
    when(lockPolicy.lockDurationFor(1)).thenReturn(Duration.ofSeconds(60));

    assertThatThrownBy(() -> handler.handle(cmd)).isInstanceOf(BadCredentialsException.class);

    ArgumentCaptor<LoginAttemptRecord> captor = ArgumentCaptor.forClass(LoginAttemptRecord.class);
    verify(lockStore).save(captor.capture());
    LoginAttemptRecord saved = captor.getValue();
    org.assertj.core.api.Assertions.assertThat(saved.failedAttempts()).isEqualTo(1);

    verify(auditLog).loginFailure(eq(USERNAME), eq(TENANT_ID), eq("Invalid credentials"), eq(1));
    verify(auditLog).accountLocked(eq(USERNAME), eq(TENANT_ID), any(Duration.class), eq(1));

    // Domain event published for failed login
    verify(eventPublisher)
        .publishEvent(new UserLoginFailedEvent(USERNAME, TENANT_ID, "Invalid credentials"));
  }

  @Test
  void handleShouldClearLockOnSuccessfulLogin() {
    LoginCommand cmd = new LoginCommand(USERNAME, "correct", TENANT_ID);
    AuthResponse response = new AuthResponse("token", "refresh", 300, "Bearer", "session-1");

    when(lockStore.find(USERNAME, TENANT_ID)).thenReturn(Optional.empty());
    when(identityService.authenticate(cmd)).thenReturn(response);
    when(identityService.requiresPasswordChange(USERNAME)).thenReturn(false);
    when(identityService.lookupId(USERNAME)).thenReturn("kc-id-1");
    when(identityService.listActiveSessions("kc-id-1")).thenReturn(List.of());

    handler.handle(cmd);

    verify(lockStore).clear(USERNAME, TENANT_ID);
    verify(auditLog).loginSuccess(eq(USERNAME), eq(TENANT_ID), any(Integer.class));

    // Domain event published for successful login
    verify(eventPublisher).publishEvent(new UserLoggedInEvent(USERNAME, TENANT_ID));
  }

  @Test
  void handleShouldFlagPasswordChangeRequiredAndAuditWithZeroSessions() {
    LoginCommand cmd = new LoginCommand(USERNAME, "correct", TENANT_ID);
    AuthResponse baseResponse = new AuthResponse("token", "refresh", 300, "Bearer", "session-1");

    when(lockStore.find(USERNAME, TENANT_ID)).thenReturn(Optional.empty());
    when(identityService.authenticate(cmd)).thenReturn(baseResponse);
    when(identityService.requiresPasswordChange(USERNAME)).thenReturn(true);

    AuthResponse result = handler.handle(cmd);

    // requiresPasswordChange flag set
    org.assertj.core.api.Assertions.assertThat(result.requiresPasswordChange()).isTrue();

    // Audit log uses 0 active sessions even when password change is required
    verify(auditLog).loginSuccess(USERNAME, TENANT_ID, 0);
  }

  @Test
  void handleShouldDetectConcurrentSessionsAndPublishEvent() {
    LoginCommand cmd = new LoginCommand(USERNAME, "correct", TENANT_ID);
    AuthResponse response = new AuthResponse("token", "refresh", 300, "Bearer", "session-1");

    when(lockStore.find(USERNAME, TENANT_ID)).thenReturn(Optional.empty());
    when(identityService.authenticate(cmd)).thenReturn(response);
    when(identityService.requiresPasswordChange(USERNAME)).thenReturn(false);

    // Two sessions returned → 1 existing besides the new one
    when(identityService.lookupId(USERNAME)).thenReturn("kc-id-1");
    when(identityService.listActiveSessions("kc-id-1"))
        .thenReturn(
            List.of(
                new UserSession("s1", "10.0.0.1", "Chrome", Instant.now(), Instant.now()),
                new UserSession("s2", "10.0.0.2", "Chrome", Instant.now(), Instant.now())));

    MDC.put("clientIp", "10.0.0.1");

    handler.handle(cmd);

    // Concurrent session audit log
    verify(auditLog).concurrentSessionDetected(USERNAME, TENANT_ID, 1, "10.0.0.1");

    // ConcurrentSessionDetectedEvent emitted with existingSessions == 1
    ArgumentCaptor<ConcurrentSessionDetectedEvent> captor =
        ArgumentCaptor.forClass(ConcurrentSessionDetectedEvent.class);
    verify(eventPublisher, times(1)).publishEvent(captor.capture());

    ConcurrentSessionDetectedEvent event = captor.getValue();
    org.assertj.core.api.Assertions.assertThat(event.username()).isEqualTo(USERNAME);
    org.assertj.core.api.Assertions.assertThat(event.tenantId()).isEqualTo(TENANT_ID);
    org.assertj.core.api.Assertions.assertThat(event.newSessionIp()).isEqualTo("10.0.0.1");
    org.assertj.core.api.Assertions.assertThat(event.existingSessionCount()).isEqualTo(1);
  }
}
