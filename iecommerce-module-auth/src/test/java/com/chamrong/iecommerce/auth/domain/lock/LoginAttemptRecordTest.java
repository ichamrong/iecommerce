package com.chamrong.iecommerce.auth.domain.lock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LoginAttemptRecordTest {

  private static final LoginLockPolicy NO_LOCK_POLICY =
      new LoginLockPolicy() {
        @Override
        public Duration lockDurationFor(int failedAttempts) {
          return Duration.ZERO;
        }

        @Override
        public int lockThreshold() {
          return 3;
        }
      };

  private static final LoginLockPolicy ONE_MINUTE_POLICY =
      new LoginLockPolicy() {
        @Override
        public Duration lockDurationFor(int failedAttempts) {
          return Duration.ofMinutes(1);
        }

        @Override
        public int lockThreshold() {
          return 3;
        }
      };

  @Test
  void cleanShouldStartWithZeroFailedAttemptsAndNotLocked() {
    LoginAttemptRecord record = LoginAttemptRecord.clean("user", "tenant");

    assertThat(record.failedAttempts()).isZero();
    assertThat(record.isLocked()).isFalse();
    assertThat(record.remainingLockDuration()).isZero();
    assertThat(record.lastAttemptAt()).isNotNull();
  }

  @Test
  void recordFailureShouldIncrementAttemptsWithoutLockWhenPolicyReturnsZero() {
    LoginAttemptRecord record = LoginAttemptRecord.clean("user", "tenant");

    LoginAttemptRecord afterFailure = record.recordFailure(NO_LOCK_POLICY);

    assertThat(afterFailure.failedAttempts()).isEqualTo(1);
    assertThat(afterFailure.isLocked()).isFalse();
    assertThat(afterFailure.remainingLockDuration()).isZero();
  }

  @Test
  void recordFailureShouldApplyLockWhenPolicyReturnsNonZero() {
    LoginAttemptRecord record = LoginAttemptRecord.clean("user", "tenant");

    LoginAttemptRecord afterFailure = record.recordFailure(ONE_MINUTE_POLICY);

    assertThat(afterFailure.failedAttempts()).isEqualTo(1);
    assertThat(afterFailure.isLocked()).isTrue();
    assertThat(afterFailure.remainingLockDuration()).isGreaterThan(Duration.ZERO);
  }

  @Test
  void isLockedShouldReturnFalseWhenLockedUntilInPastOrNull() {
    LoginAttemptRecord notLocked = new LoginAttemptRecord("user", "tenant", 1, null, Instant.now());
    LoginAttemptRecord expiredLock =
        new LoginAttemptRecord("user", "tenant", 1, Instant.now().minusSeconds(5), Instant.now());

    assertThat(notLocked.isLocked()).isFalse();
    assertThat(expiredLock.isLocked()).isFalse();
  }

  @Test
  void recordSuccessShouldResetToCleanState() {
    LoginAttemptRecord locked =
        new LoginAttemptRecord("user", "tenant", 5, Instant.now().plusSeconds(60), Instant.now());

    LoginAttemptRecord afterSuccess = locked.recordSuccess();

    assertThat(afterSuccess.failedAttempts()).isZero();
    assertThat(afterSuccess.isLocked()).isFalse();
  }
}
