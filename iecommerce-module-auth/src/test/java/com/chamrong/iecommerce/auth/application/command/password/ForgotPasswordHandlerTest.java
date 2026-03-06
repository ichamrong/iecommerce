package com.chamrong.iecommerce.auth.application.command.password;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.audit.AuthEventLogger;
import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.UserSession;
import com.chamrong.iecommerce.auth.domain.lock.LoginLockStore;
import com.chamrong.iecommerce.auth.domain.ports.UserRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordHandlerTest {

  @Mock private UserRepositoryPort userRepository;
  @Mock private IdentityService identityService;
  @Mock private LoginLockStore lockStore;
  @Mock private AuthEventLogger auditLog;

  @InjectMocks private ForgotPasswordHandler handler;

  @Test
  void handleShouldTriggerResetRevokeSessionsClearLockAndAuditWhenUserExists() {
    var cmd = new ForgotPasswordCommand("user@example.com", "TENANT");
    var user = new User("TENANT", "user", "user@example.com");
    user.linkKeycloak("kc-id");

    when(userRepository.findByEmailAndTenantId(cmd.email(), cmd.tenantId()))
        .thenReturn(Optional.of(user));

    List<UserSession> sessions =
        List.of(new UserSession("s1", "127.0.0.1", "Chrome", Instant.now(), Instant.now()));
    when(identityService.listActiveSessions("kc-id")).thenReturn(sessions);

    handler.handle(cmd);

    verify(identityService).sendPasswordResetEmail("kc-id");
    verify(identityService).listActiveSessions("kc-id");
    verify(identityService).revokeAllSessions("kc-id");
    verify(lockStore).clear("user", "TENANT");
    verify(auditLog).sessionsInvalidated("user", "TENANT", sessions.size(), "forgot-password");
    verify(auditLog).forgotPasswordTriggered("TENANT", true);
  }

  @Test
  void handleShouldBeSilentNoopWhenUserDoesNotExist() {
    var cmd = new ForgotPasswordCommand("missing@example.com", "TENANT");
    when(userRepository.findByEmailAndTenantId(cmd.email(), cmd.tenantId()))
        .thenReturn(Optional.empty());

    handler.handle(cmd);

    verify(identityService, never()).sendPasswordResetEmail(any());
    verify(identityService, never()).revokeAllSessions(any());
    verify(lockStore, never()).clear(any(), any());
    verify(auditLog).forgotPasswordTriggered("TENANT", false);
  }
}
