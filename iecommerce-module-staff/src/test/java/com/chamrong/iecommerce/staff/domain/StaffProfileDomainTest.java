package com.chamrong.iecommerce.staff.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StaffProfileDomainTest {

  private StaffProfile staff;

  @BeforeEach
  void setUp() {
    staff = new StaffProfile("user1", "Alice Smith", StaffRole.SUPPORT);
  }

  // ── Status transitions ────────────────────────────────────────────────────

  @Test
  void shouldBeActiveOnCreation() {
    assertEquals(StaffStatus.ACTIVE, staff.getStatus());
  }

  @Test
  void shouldSuspendActiveStaff() {
    staff.suspend();
    assertEquals(StaffStatus.SUSPENDED, staff.getStatus());
  }

  @Test
  void shouldReactivateSuspendedStaff() {
    staff.suspend();
    staff.reactivate();
    assertEquals(StaffStatus.ACTIVE, staff.getStatus());
  }

  @Test
  void shouldTerminateActiveStaff() {
    staff.terminate();
    assertEquals(StaffStatus.TERMINATED, staff.getStatus());
    assertNotNull(staff.getTerminationDate());
  }

  @Test
  void shouldTerminateSuspendedStaff() {
    staff.suspend();
    staff.terminate();
    assertEquals(StaffStatus.TERMINATED, staff.getStatus());
  }

  // ── Invariants ────────────────────────────────────────────────────────────

  @Test
  void shouldRejectSuspendOnTerminatedStaff() {
    staff.terminate();
    assertThrows(IllegalStateException.class, staff::suspend);
  }

  @Test
  void shouldRejectReactivateOnTerminatedStaff() {
    staff.terminate();
    assertThrows(IllegalStateException.class, staff::reactivate);
  }

  @Test
  void shouldRejectProfileUpdateOnTerminatedStaff() {
    staff.terminate();
    assertThrows(
        IllegalStateException.class, () -> staff.updateProfile("New Name", "0000", "HR", "HQ"));
  }

  @Test
  void shouldAllowProfileUpdateOnSuspendedStaff() {
    staff.suspend();
    // Suspended staff can still have profile data corrected
    assertDoesNotThrow(() -> staff.updateProfile("Fixed Name", null, null, null));
    assertEquals("Fixed Name", staff.getFullName());
  }

  @Test
  void shouldEnforceDefaultRoleOnCreate() {
    var profile = new StaffProfile("user2", "Bob", null);
    assertEquals(StaffRole.SUPPORT, profile.getRole());
  }
}
