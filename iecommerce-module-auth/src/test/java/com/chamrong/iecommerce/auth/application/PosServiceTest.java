package com.chamrong.iecommerce.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.domain.PosSession;
import com.chamrong.iecommerce.auth.domain.PosTerminal;
import com.chamrong.iecommerce.auth.domain.ports.PosSessionRepositoryPort;
import com.chamrong.iecommerce.auth.domain.ports.PosTerminalRepositoryPort;
import com.chamrong.iecommerce.auth.testsupport.AuthTestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link PosService}. */
@ExtendWith(MockitoExtension.class)
class PosServiceTest {

  private static final String TENANT_ID = "TENANT-1";

  @Mock private PosTerminalRepositoryPort terminalRepository;
  @Mock private PosSessionRepositoryPort sessionRepository;

  @InjectMocks private PosService posService;

  @Test
  void registerTerminalShouldPersistTerminal() {
    PosTerminal terminal = AuthTestDataFactory.terminal(TENANT_ID);
    ArgumentCaptor<PosTerminal> terminalCaptor = ArgumentCaptor.forClass(PosTerminal.class);

    when(terminalRepository.save(terminalCaptor.capture())).thenReturn(terminal);

    PosTerminal result = posService.registerTerminal(TENANT_ID, "Main POS", "HW-1", "BR-1");

    PosTerminal saved = terminalCaptor.getValue();
    assertThat(saved.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(saved.getName()).isEqualTo("Main POS");
    assertThat(saved.getHardwareId()).isEqualTo("HW-1");
    assertThat(saved.getBranchId()).isEqualTo("BR-1");

    assertThat(result).isSameAs(terminal);
  }

  @Test
  void openSessionShouldThrowWhenTerminalNotFound() {
    when(terminalRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> posService.openSession(TENANT_ID, 1L, 10L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Terminal not found");

    verify(sessionRepository, never()).save(any(PosSession.class));
  }

  @Test
  void openSessionShouldThrowWhenTerminalInactive() {
    PosTerminal terminal = AuthTestDataFactory.terminal(TENANT_ID);
    terminal.deactivate();
    when(terminalRepository.findById(1L)).thenReturn(Optional.of(terminal));

    assertThatThrownBy(() -> posService.openSession(TENANT_ID, 1L, 10L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Terminal is not active");
  }

  @Test
  void openSessionShouldClosePreviousActiveSessionAndCreateNew() {
    PosTerminal terminal = AuthTestDataFactory.terminal(TENANT_ID);
    when(terminalRepository.findById(1L)).thenReturn(Optional.of(terminal));

    PosSession existing = AuthTestDataFactory.activeSession(TENANT_ID, 1L, 10L);
    when(sessionRepository.findActiveSession(1L, 10L)).thenReturn(Optional.of(existing));

    ArgumentCaptor<PosSession> sessionCaptor = ArgumentCaptor.forClass(PosSession.class);
    when(sessionRepository.save(sessionCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PosSession created = posService.openSession(TENANT_ID, 1L, 10L);

    // First save: existing session closed
    PosSession firstSaved = sessionCaptor.getAllValues().get(0);
    assertThat(firstSaved.isActive()).isFalse();
    assertThat(firstSaved.getClosingNotes()).isEqualTo("Auto-closed by new login");

    // Second save: new session created
    PosSession secondSaved = sessionCaptor.getAllValues().get(1);
    assertThat(secondSaved.getTerminalId()).isEqualTo(1L);
    assertThat(secondSaved.getCashierId()).isEqualTo(10L);
    assertThat(created).isSameAs(secondSaved);
  }

  @Test
  void closeSessionShouldCloseAndPersistSession() {
    PosSession session = AuthTestDataFactory.activeSession(TENANT_ID, 1L, 10L);
    when(sessionRepository.findById(5L)).thenReturn(Optional.of(session));

    ArgumentCaptor<PosSession> sessionCaptor = ArgumentCaptor.forClass(PosSession.class);
    when(sessionRepository.save(sessionCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    posService.closeSession(5L, "End of shift");

    PosSession saved = sessionCaptor.getValue();
    assertThat(saved.isActive()).isFalse();
    assertThat(saved.getClosingNotes()).isEqualTo("End of shift");
  }

  @Test
  void listTerminalsShouldDelegateToRepository() {
    PosTerminal t1 = AuthTestDataFactory.terminal(TENANT_ID);
    when(terminalRepository.findByTenantId(TENANT_ID)).thenReturn(List.of(t1));

    List<PosTerminal> result = posService.listTerminals(TENANT_ID);

    assertThat(result).containsExactly(t1);
  }
}
