package com.chamrong.iecommerce.auth.infrastructure.security;

import com.chamrong.iecommerce.auth.domain.Permissions;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Converts a Keycloak JWT into an {@link AbstractAuthenticationToken} with realm roles and
 * permission authorities. Realm roles (e.g. ROLE_PLATFORM_ADMIN) are added as-is; for
 * platform-level roles we also add fine-grained permission authorities so that
 * {@code @PreAuthorize("hasAuthority('reports:read')")} and similar checks pass without requiring
 * permissions in the JWT itself.
 */
public class KeycloakJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String ROLE_PLATFORM_ADMIN = "ROLE_PLATFORM_ADMIN";

  /** Permissions granted to ROLE_PLATFORM_ADMIN for method security (@PreAuthorize). */
  private static final Set<String> PLATFORM_ADMIN_PERMISSIONS =
      Set.of(
          Permissions.USER_READ,
          Permissions.USER_CREATE,
          Permissions.USER_DISABLE,
          Permissions.TENANT_CREATE,
          Permissions.STAFF_MANAGE,
          Permissions.AUDIT_READ,
          Permissions.AUDIT_WRITE,
          Permissions.SALE_READ,
          Permissions.SALE_MANAGE,
          Permissions.INVOICE_READ,
          Permissions.INVOICE_MANAGE,
          Permissions.PROFILE_READ,
          Permissions.EKYC_READ,
          Permissions.EKYC_REVIEW,
          Permissions.HELPDESK_READ,
          Permissions.HELPDESK_REPLY,
          Permissions.FINANCE_MANAGE,
          "reports:read",
          "platform:admin",
          "bookings:manage",
          "reviews:moderate",
          "reviews:manage",
          "promotions:read",
          "promotions:manage",
          "orders:manage",
          "notifications:manage",
          "assets:read",
          "assets:manage",
          "settings:read",
          "settings:manage");

  private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter =
      new JwtGrantedAuthoritiesConverter();

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities =
        Stream.concat(
                defaultGrantedAuthoritiesConverter.convert(jwt).stream(),
                Stream.concat(
                    extractRealmRoles(jwt).stream(),
                    permissionsForRealmRoles(extractRealmRoleNames(jwt)).stream()))
            .collect(Collectors.toSet());

    return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
  }

  private String getPrincipalClaimName(Jwt jwt) {
    String claimName = "preferred_username";
    return jwt.hasClaim(claimName) ? jwt.getClaimAsString(claimName) : jwt.getSubject();
  }

  private Collection<String> extractRealmRoleNames(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess == null || !realmAccess.containsKey("roles")) {
      return Collections.emptyList();
    }
    @SuppressWarnings("unchecked")
    Collection<String> roles = (Collection<String>) realmAccess.get("roles");
    return roles == null ? Collections.emptyList() : roles;
  }

  private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
    return extractRealmRoleNames(jwt).stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }

  /**
   * Returns permission authorities for platform-level roles so that @PreAuthorize permission checks
   * pass when the JWT only contains realm roles.
   */
  private Collection<GrantedAuthority> permissionsForRealmRoles(Collection<String> roleNames) {
    if (roleNames == null || !roleNames.contains(ROLE_PLATFORM_ADMIN)) {
      return Collections.emptySet();
    }
    return PLATFORM_ADMIN_PERMISSIONS.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }
}
