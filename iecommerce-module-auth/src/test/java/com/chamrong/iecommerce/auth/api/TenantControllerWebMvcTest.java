package com.chamrong.iecommerce.auth.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chamrong.iecommerce.auth.application.command.TenantSignupCommand;
import com.chamrong.iecommerce.auth.application.command.UpdateTenantStatusCommand;
import com.chamrong.iecommerce.auth.application.command.tenant.TenantProvisionHandler;
import com.chamrong.iecommerce.auth.application.command.tenant.TenantSignupHandler;
import com.chamrong.iecommerce.auth.application.command.tenant.UpdateTenantPreferencesHandler;
import com.chamrong.iecommerce.auth.application.command.tenant.UpdateTenantStatusHandler;
import com.chamrong.iecommerce.auth.application.dto.TenantResponse;
import com.chamrong.iecommerce.auth.application.query.GetTenantByCodeHandler;
import com.chamrong.iecommerce.auth.application.query.GetTenantPreferencesHandler;
import com.chamrong.iecommerce.auth.application.query.ListTenantsHandler;
import com.chamrong.iecommerce.auth.domain.TenantPlan;
import com.chamrong.iecommerce.auth.domain.TenantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Standalone MVC tests for {@link TenantController} using mocked handlers. */
@ExtendWith(MockitoExtension.class)
class TenantControllerWebMvcTest {

  private MockMvc mockMvc;

  @Mock private TenantSignupHandler signupHandler;
  @Mock private TenantProvisionHandler provisionHandler;
  @Mock private UpdateTenantStatusHandler statusHandler;
  @Mock private UpdateTenantPreferencesHandler updatePreferencesHandler;
  @Mock private GetTenantPreferencesHandler getPreferencesHandler;
  @Mock private ListTenantsHandler listTenantsHandler;
  @Mock private GetTenantByCodeHandler getTenantByCodeHandler;

  @Mock
  private com.chamrong.iecommerce.auth.application.command.tenant.UpdateTenantHandler
      updateTenantHandler;

  @BeforeEach
  void setUp() {
    TenantController controller =
        new TenantController(
            signupHandler,
            provisionHandler,
            statusHandler,
            updatePreferencesHandler,
            getPreferencesHandler,
            listTenantsHandler,
            getTenantByCodeHandler,
            updateTenantHandler);
    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void selfServiceSignupShouldReturnCreated() throws Exception {
    when(signupHandler.handle(any(TenantSignupCommand.class)))
        .thenReturn(
            new TenantResponse(
                "tenant-1",
                "My Shop",
                TenantPlan.FREE,
                TenantStatus.TRIAL,
                "owner@example.com",
                null));

    String body =
        """
        {
          "shopName": "My Shop",
          "ownerUsername": "owner",
          "ownerEmail": "owner@example.com",
          "ownerPassword": "secret"
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/tenants/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
  }

  @Test
  void adminUpdateStatusShouldReturnNotFoundWhenHandlerThrowsIllegalArgumentException()
      throws Exception {
    doThrow(new IllegalArgumentException("Tenant not found"))
        .when(statusHandler)
        .handle(any(UpdateTenantStatusCommand.class));

    String body =
        """
        {
          "tenantId": "missing",
          "status": "SUSPENDED"
        }
        """;

    mockMvc
        .perform(
            put("/api/v1/admin/tenants/{id}/status", "missing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isNotFound());
  }
}
