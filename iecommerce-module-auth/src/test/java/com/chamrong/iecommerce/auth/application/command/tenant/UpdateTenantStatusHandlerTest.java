package com.chamrong.iecommerce.auth.application.command.tenant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.command.UpdateTenantStatusCommand;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import com.chamrong.iecommerce.auth.domain.event.TenantStatusUpdatedEvent;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class UpdateTenantStatusHandlerTest {

  @Mock private TenantRepositoryPort tenantRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private UpdateTenantStatusHandler handler;

  @Test
  void handleShouldUpdateStatusAndPublishEvent() {
    var cmd = new UpdateTenantStatusCommand("tenant-1", TenantStatus.SUSPENDED);
    Tenant tenant = new Tenant("tenant-1", "Tenant One");

    when(tenantRepository.findByCode("tenant-1")).thenReturn(Optional.of(tenant));

    handler.handle(cmd);

    // Tenant status updated and saved
    org.assertj.core.api.Assertions.assertThat(tenant.getStatus())
        .isEqualTo(TenantStatus.SUSPENDED);
    verify(tenantRepository).save(tenant);

    // Event published
    ArgumentCaptor<TenantStatusUpdatedEvent> captor =
        ArgumentCaptor.forClass(TenantStatusUpdatedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());

    TenantStatusUpdatedEvent event = captor.getValue();
    org.assertj.core.api.Assertions.assertThat(event.tenantCode()).isEqualTo("tenant-1");
    org.assertj.core.api.Assertions.assertThat(event.status()).isEqualTo(TenantStatus.SUSPENDED);
  }

  @Test
  void handleShouldThrowWhenTenantNotFound() {
    var cmd = new UpdateTenantStatusCommand("missing", TenantStatus.ACTIVE);
    when(tenantRepository.findByCode("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Tenant not found");

    verify(eventPublisher, org.mockito.Mockito.never()).publishEvent(any());
  }
}
