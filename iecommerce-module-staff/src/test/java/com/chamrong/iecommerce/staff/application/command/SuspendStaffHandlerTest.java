package com.chamrong.iecommerce.staff.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.staff.domain.StaffAuditLogPort;
import com.chamrong.iecommerce.staff.domain.StaffProfile;
import com.chamrong.iecommerce.staff.domain.StaffRepositoryPort;
import com.chamrong.iecommerce.staff.domain.StaffRole;
import com.chamrong.iecommerce.staff.domain.StaffStatus;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SuspendStaffHandlerTest {

  @Mock private StaffRepositoryPort staffRepository;
  @Mock private StaffAuditLogPort auditLogPort;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private SuspendStaffHandler sut;

  private StaffProfile activeStaff;

  @BeforeEach
  void setUp() {
    activeStaff = new StaffProfile("user1", "Alice", StaffRole.SUPPORT);
    // Inject ID via reflection since it's set by JPA
    org.springframework.test.util.ReflectionTestUtils.setField(activeStaff, "id", 10L);
  }

  @Test
  @DisplayName("Should suspend an active staff member and write audit log")
  void shouldSuspendActiveStaff() {
    when(staffRepository.findById(10L)).thenReturn(Optional.of(activeStaff));
    when(staffRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    sut.handle(10L);

    assertEquals(StaffStatus.SUSPENDED, activeStaff.getStatus());
    verify(auditLogPort)
        .save(
            argThat(
                log ->
                    "STAFF_SUSPENDED".equals(log.getActionType())
                        && log.getTargetStaffId() == 10L));
    verify(eventPublisher)
        .publishEvent(any(com.chamrong.iecommerce.staff.StaffSuspendedEvent.class));
  }

  @Test
  @DisplayName("Should throw EntityNotFoundException when staff not found")
  void shouldThrowWhenStaffNotFound() {
    when(staffRepository.findById(99L)).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> sut.handle(99L));
    verifyNoInteractions(auditLogPort, eventPublisher);
  }

  @Test
  @DisplayName("Should reject suspending an already-terminated staff member")
  void shouldRejectSuspendOnTerminatedStaff() {
    activeStaff.terminate();
    when(staffRepository.findById(10L)).thenReturn(Optional.of(activeStaff));

    assertThrows(IllegalStateException.class, () -> sut.handle(10L));
    verifyNoInteractions(auditLogPort);
  }
}
