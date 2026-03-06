package com.chamrong.iecommerce.auth.infrastructure.lock;

import static org.assertj.core.api.Assertions.assertThat;

import com.chamrong.iecommerce.auth.domain.lock.LoginAttemptRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemoryLoginLockStore}. */
class InMemoryLoginLockStoreTest {

  private static final String USERNAME = "user";
  private static final String TENANT_ID = "TENANT-1";

  private final InMemoryLoginLockStore store = new InMemoryLoginLockStore();

  @Test
  void saveAndFindShouldRoundTripRecord() {
    LoginAttemptRecord record =
        new LoginAttemptRecord(
            USERNAME, TENANT_ID, 3, Instant.now().plus(Duration.ofMinutes(5)), Instant.now());

    store.save(record);

    Optional<LoginAttemptRecord> found = store.find(USERNAME, TENANT_ID);

    assertThat(found).isPresent();
    assertThat(found.get().failedAttempts()).isEqualTo(3);
  }

  @Test
  void clearShouldRemoveRecord() {
    LoginAttemptRecord record = new LoginAttemptRecord(USERNAME, TENANT_ID, 1, null, Instant.now());

    store.save(record);
    store.clear(USERNAME, TENANT_ID);

    assertThat(store.find(USERNAME, TENANT_ID)).isEmpty();
  }
}
