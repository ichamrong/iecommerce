package com.chamrong.iecommerce.staff.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chamrong.iecommerce.staff.application.StaffMapper;
import com.chamrong.iecommerce.staff.domain.StaffAccessDeniedException;
import com.chamrong.iecommerce.staff.domain.StaffProfile;
import com.chamrong.iecommerce.staff.domain.StaffRepositoryPort;
import com.chamrong.iecommerce.staff.domain.StaffRole;
import com.chamrong.iecommerce.staff.domain.StaffStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StaffQueryHandlerTenantAccessTest {

  @Test
  void deniesAccessWhenTenantNotAssigned() {
    StaffProfile profile = new StaffProfile("user1", "Alice", StaffRole.SUPPORT);
    profile.getAssignedTenants().add("tenant-1");

    StaffRepositoryPort repo =
        new StaffRepositoryPort() {
          @Override
          public Optional<StaffProfile> findByUserId(String userId) {
            return Optional.empty();
          }

          @Override
          public boolean existsByUserId(String userId) {
            return false;
          }

          @Override
          public Optional<StaffProfile> findById(Long id) {
            return Optional.of(profile);
          }

          @Override
          public StaffProfile save(StaffProfile staff) {
            return staff;
          }

          @Override
          public List<StaffProfile> findNextPage(Instant createdAt, Long id, int limit) {
            return List.of();
          }
        };

    StaffQueryHandler handler = new StaffQueryHandler(repo, new StaffMapper());

    assertThatThrownBy(() -> handler.findById("other-tenant", 1L))
        .isInstanceOf(StaffAccessDeniedException.class);
  }

  @Test
  void allowsAccessWhenTenantAssigned() {
    StaffProfile profile = new StaffProfile("user1", "Alice", StaffRole.SUPPORT);
    profile.setId(1L);
    profile.setStatus(StaffStatus.ACTIVE);
    profile.getAssignedTenants().add("tenant-1");

    StaffRepositoryPort repo =
        new StaffRepositoryPort() {
          @Override
          public Optional<StaffProfile> findByUserId(String userId) {
            return Optional.empty();
          }

          @Override
          public boolean existsByUserId(String userId) {
            return false;
          }

          @Override
          public Optional<StaffProfile> findById(Long id) {
            return Optional.of(profile);
          }

          @Override
          public StaffProfile save(StaffProfile staff) {
            return staff;
          }

          @Override
          public List<StaffProfile> findNextPage(Instant createdAt, Long id, int limit) {
            return List.of();
          }
        };

    StaffQueryHandler handler = new StaffQueryHandler(repo, new StaffMapper());

    var response = handler.findById("tenant-1", 1L);
    assertThat(response.id()).isNotNull();
    assertThat(response.fullName()).isEqualTo("Alice");
  }
}
