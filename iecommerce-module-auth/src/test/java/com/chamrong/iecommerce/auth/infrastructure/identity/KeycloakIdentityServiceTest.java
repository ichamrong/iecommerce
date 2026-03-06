package com.chamrong.iecommerce.auth.infrastructure.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chamrong.iecommerce.auth.application.command.RegisterCommand;
import com.chamrong.iecommerce.auth.domain.idp.IdentityProviderType;
import com.chamrong.iecommerce.auth.domain.idp.SocialProvider;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.IdentityProvidersResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Focused tests for {@link KeycloakIdentityService} that exercise Keycloak admin client
 * interactions without performing real HTTP calls.
 */
@ExtendWith(MockitoExtension.class)
class KeycloakIdentityServiceTest {

  @Mock private Keycloak keycloak;
  @Mock private RealmResource realm;
  @Mock private UsersResource usersResource;
  @Mock private IdentityProvidersResource idpResource;

  @Mock private Response response;

  private KeycloakIdentityService service;

  @BeforeEach
  void setUp() {
    KeycloakProperties props = new KeycloakProperties();
    props.setServerUrl("http://idp");
    props.setRealm("realm-1");
    KeycloakProperties.ClientProperties clients = new KeycloakProperties.ClientProperties();
    clients.setWeb("web-client");
    props.setClients(clients);

    lenient().when(keycloak.realm("realm-1")).thenReturn(realm);
    lenient().when(realm.users()).thenReturn(usersResource);
    lenient().when(realm.identityProviders()).thenReturn(idpResource);

    service = new KeycloakIdentityService(keycloak, props);
  }

  @Test
  void registerUserShouldReturnKeycloakIdWhenCreationSucceeds() {
    RegisterCommand cmd =
        new RegisterCommand("alice", "alice@example.com", "password", "TENANT-1", null);

    when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
    when(response.getStatus()).thenReturn(201);
    when(response.getLocation())
        .thenReturn(URI.create("http://idp/admin/realms/realm-1/users/12345"));

    String keycloakId = service.registerUser(cmd);

    assertThat(keycloakId).isEqualTo("12345");
    verify(usersResource).create(any(UserRepresentation.class));
  }

  @Test
  void registerUserShouldThrowWhenUserAlreadyExists() {
    RegisterCommand cmd =
        new RegisterCommand("alice", "alice@example.com", "password", "TENANT-1", null);

    when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
    when(response.getStatus()).thenReturn(409);

    assertThatThrownBy(() -> service.registerUser(cmd))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("User already exists in Identity Provider");
  }

  @Test
  void requiresPasswordChangeShouldReflectRequiredActions() {
    UserRepresentation user = new UserRepresentation();
    user.setRequiredActions(List.of("UPDATE_PASSWORD"));

    when(usersResource.search("alice@example.com", true)).thenReturn(List.of(user));

    boolean result = service.requiresPasswordChange("alice@example.com");

    assertThat(result).isTrue();
  }

  @Test
  void listSocialProvidersShouldMapEnabledProviders() {
    IdentityProviderRepresentation google = new IdentityProviderRepresentation();
    google.setProviderId("google");
    google.setAlias("google");
    google.setDisplayName("Google");
    google.setEnabled(true);

    IdentityProviderRepresentation disabled = new IdentityProviderRepresentation();
    disabled.setProviderId("github");
    disabled.setAlias("github");
    disabled.setEnabled(false);

    when(idpResource.findAll()).thenReturn(List.of(google, disabled));

    List<SocialProvider> providers = service.listSocialProviders();

    assertThat(providers).hasSize(1);
    SocialProvider provider = providers.getFirst();
    assertThat(provider.type()).isEqualTo(IdentityProviderType.GOOGLE);
    assertThat(provider.alias()).isEqualTo("google");
    assertThat(provider.displayName()).isEqualTo("Google");
    assertThat(provider.enabled()).isTrue();
  }
}
