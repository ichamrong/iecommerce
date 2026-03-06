package com.chamrong.iecommerce.auth.domain.lock;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.auth.infrastructure.config.LoginLockConfig;
import com.chamrong.iecommerce.auth.infrastructure.config.LoginLockProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoginLockPolicyTest {

  private final LoginLockConfig config = new LoginLockConfig();

  @Test
  void shouldNotLockBeforeThreshold() {
    LoginLockProperties props = new LoginLockProperties("memory", 3, List.of(60, 180, 300));
    LoginLockPolicy policy = config.loginLockPolicy(props);

    assertThat(policy.lockDurationFor(1)).isEqualTo(Duration.ZERO);
    assertThat(policy.lockDurationFor(2)).isEqualTo(Duration.ZERO);
  }

  @Test
  void shouldApplyProgressiveDurationsAndCapAtLastStep() {
    LoginLockProperties props = new LoginLockProperties("memory", 3, List.of(60, 180, 300));
    LoginLockPolicy policy = config.loginLockPolicy(props);

    assertThat(policy.lockDurationFor(3)).isEqualTo(Duration.ofSeconds(60));
    assertThat(policy.lockDurationFor(4)).isEqualTo(Duration.ofSeconds(180));
    assertThat(policy.lockDurationFor(5)).isEqualTo(Duration.ofSeconds(300));
    assertThat(policy.lockDurationFor(6)).isEqualTo(Duration.ofSeconds(300));
    assertThat(policy.lockDurationFor(7)).isEqualTo(Duration.ofSeconds(300));
    assertThat(policy.lockDurationFor(10)).isEqualTo(Duration.ofSeconds(300));
  }

  @Test
  void shouldRespectThresholdAndLockThresholdAccessor() {
    LoginLockProperties props = new LoginLockProperties("memory", 5, List.of(60));
    LoginLockPolicy policy = config.loginLockPolicy(props);

    // Below threshold → no lock
    assertThat(policy.lockDurationFor(1)).isEqualTo(Duration.ZERO);
    assertThat(policy.lockDurationFor(4)).isEqualTo(Duration.ZERO);

    // At and above threshold → always same duration because only one step defined
    assertThat(policy.lockDurationFor(5)).isEqualTo(Duration.ofSeconds(60));
    assertThat(policy.lockDurationFor(10)).isEqualTo(Duration.ofSeconds(60));

    // lockThreshold accessor exposes configured threshold
    assertThat(policy.lockThreshold()).isEqualTo(5);
  }
}
