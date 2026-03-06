package com.chamrong.iecommerce.auth.application.command.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.command.UpdateTenantPreferencesCommand;
import com.chamrong.iecommerce.auth.application.dto.TenantPreferencesResponse;
import com.chamrong.iecommerce.auth.domain.Tenant;
import com.chamrong.iecommerce.auth.domain.TenantPreferences;
import com.chamrong.iecommerce.auth.domain.event.TenantPreferencesUpdatedEvent;
import com.chamrong.iecommerce.auth.domain.ports.TenantRepositoryPort;
import com.chamrong.iecommerce.auth.testsupport.AuthTestDataFactory;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Unit tests for {@link UpdateTenantPreferencesHandler}. */
@ExtendWith(MockitoExtension.class)
class UpdateTenantPreferencesHandlerTest {

  private static final String TENANT_ID = "TENANT-1";

  @Mock private TenantRepositoryPort tenantRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private UpdateTenantPreferencesHandler handler;

  @Test
  void handleShouldThrowWhenTenantNotFound() {
    UpdateTenantPreferencesCommand cmd =
        new UpdateTenantPreferencesCommand(TENANT_ID, null, null, null, null);

    when(tenantRepository.findByCode(TENANT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.handle(cmd))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Tenant not found");
  }

  @Test
  void handleShouldUpdateOnlyNonNullFieldsAndPublishEvent() {
    Tenant tenant = AuthTestDataFactory.tenant(TENANT_ID);
    TenantPreferences prefs = tenant.getPreferences();
    prefs.setLogoUrl("old-logo");
    prefs.setPrimaryColor("old-primary");
    prefs.setSecondaryColor("old-secondary");
    prefs.setFontFamily("Old Font");

    when(tenantRepository.findByCode(TENANT_ID)).thenReturn(Optional.of(tenant));

    UpdateTenantPreferencesCommand cmd =
        new UpdateTenantPreferencesCommand(TENANT_ID, "new-logo", null, "new-secondary", null);

    ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
    when(tenantRepository.save(tenantCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ArgumentCaptor<TenantPreferencesUpdatedEvent> eventCaptor =
        ArgumentCaptor.forClass(TenantPreferencesUpdatedEvent.class);

    TenantPreferencesResponse response = handler.handle(cmd);

    // Preferences updated selectively
    Tenant saved = tenantCaptor.getValue();
    TenantPreferences updated = saved.getPreferences();

    assertThat(updated.getLogoUrl()).isEqualTo("new-logo");
    assertThat(updated.getSecondaryColor()).isEqualTo("new-secondary");
    // unchanged fields remain as before
    assertThat(updated.getPrimaryColor()).isEqualTo("old-primary");
    assertThat(updated.getFontFamily()).isEqualTo("Old Font");

    // Response mirrors updated preferences
    assertThat(response.logoUrl()).isEqualTo("new-logo");
    assertThat(response.secondaryColor()).isEqualTo("new-secondary");

    verify(eventPublisher).publishEvent(eventCaptor.capture());
    TenantPreferencesUpdatedEvent event = eventCaptor.getValue();
    assertThat(event.tenantCode()).isEqualTo(TENANT_ID);
  }
}
