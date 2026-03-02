package com.chamrong.iecommerce.customer.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.customer.application.dto.AuthTokens;
import com.chamrong.iecommerce.customer.domain.Customer;
import com.chamrong.iecommerce.customer.domain.auth.AccountState;
import com.chamrong.iecommerce.customer.domain.auth.DefaultLoginLockPolicy;
import com.chamrong.iecommerce.customer.domain.auth.LoginLockPolicy;
import com.chamrong.iecommerce.customer.domain.auth.port.CustomerCredentialPort;
import com.chamrong.iecommerce.customer.domain.auth.port.LoginAttemptPort;
import com.chamrong.iecommerce.customer.domain.auth.port.SessionStorePort;
import com.chamrong.iecommerce.customer.domain.ports.CustomerRepositoryPort;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LoginCustomerHandlerTest {

  @Mock private CustomerRepositoryPort customerRepository;
  @Mock private CustomerCredentialPort credentialPort;
  @Mock private LoginAttemptPort attemptPort;
  @Mock private SessionStorePort sessionStorePort;

  @Spy private LoginLockPolicy lockPolicy = new DefaultLoginLockPolicy();

  @InjectMocks private LoginCustomerHandler sut;

  private Customer testCustomer;
  private final String email = "test@example.com";
  private final String password = "password";

  @BeforeEach
  void setUp() {
    testCustomer = new Customer();
    ReflectionTestUtils.setField(testCustomer, "id", 1L);
    testCustomer.setEmail(email);

    // Mock concurrentPolicy which is hardcoded in the original class
    // Doing nothing, defaults to ConcurrentLoginPolicy.INVALIDATE_OLD
  }

  private static final String TENANT_ID = "tenant-1";

  @Test
  void shouldLoginSuccessfullyAndInvalidateOldSessions() {
    LoginCommand cmd = new LoginCommand(TENANT_ID, email, password, "iPhone");
    when(customerRepository.findByTenantIdAndEmail(TENANT_ID, email))
        .thenReturn(Optional.of(testCustomer));
    when(attemptPort.getAccountState("1")).thenReturn(new AccountState("1", 0, null));
    when(credentialPort.verify("1", password)).thenReturn(true);
    when(credentialPort.generateTokens(eq("1"), eq(1L), any()))
        .thenReturn(new AuthTokens("access", "refresh"));

    AuthTokens tokens = sut.handle(cmd);

    assertNotNull(tokens);
    assertEquals("access", tokens.accessToken());

    // Verify attempt counter reset
    verify(attemptPort).saveAccountState(any());

    // Verify session store interactions
    verify(sessionStorePort).invalidateOtherSessions(eq("1"), any());
    verify(sessionStorePort).registerSession(eq("1"), any(), eq("iPhone"));
  }

  @Test
  void shouldLockAccountAfterThresholdFailures() {
    LoginCommand cmd = new LoginCommand(TENANT_ID, email, "wrong", "iPhone");
    when(customerRepository.findByTenantIdAndEmail(TENANT_ID, email))
        .thenReturn(Optional.of(testCustomer));

    // Simulating 2 failures currently
    AccountState state = new AccountState("1", 2, null);
    when(attemptPort.getAccountState("1")).thenReturn(state);
    when(credentialPort.verify("1", "wrong")).thenReturn(false);

    Exception ex = assertThrows(RuntimeException.class, () -> sut.handle(cmd));
    assertEquals("Bad credentials", ex.getMessage());

    // After this 3rd failure, it should be locked
    assertEquals(3, state.getConsecutiveFailures());
    assertNotNull(state.getLockedUntil());
    verify(attemptPort).saveAccountState(state);
  }

  @Test
  void shouldRejectLoginIfAccountIsLocked() {
    LoginCommand cmd = new LoginCommand(TENANT_ID, email, password, "iPhone");
    when(customerRepository.findByTenantIdAndEmail(TENANT_ID, email))
        .thenReturn(Optional.of(testCustomer));

    // Account locked until 5 mins from now
    AccountState state = new AccountState("1", 7, Instant.now().plusSeconds(300));
    when(attemptPort.getAccountState("1")).thenReturn(state);

    Exception ex = assertThrows(RuntimeException.class, () -> sut.handle(cmd));
    assertNotNull(ex); // "Account locked until ..."
  }
}
