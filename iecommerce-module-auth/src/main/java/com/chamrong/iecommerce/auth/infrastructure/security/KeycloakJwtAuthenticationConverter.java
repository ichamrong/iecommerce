package com.chamrong.iecommerce.auth.infrastructure.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class KeycloakJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter =
      new JwtGrantedAuthoritiesConverter();

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities =
        Stream.concat(
                defaultGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractRealmRoles(jwt).stream())
            .collect(Collectors.toSet());

    return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
  }

  private String getPrincipalClaimName(Jwt jwt) {
    String claimName = "preferred_username";
    return jwt.hasClaim(claimName) ? jwt.getClaimAsString(claimName) : jwt.getSubject();
  }

  private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess == null || !realmAccess.containsKey("roles")) {
      return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    Collection<String> roles = (Collection<String>) realmAccess.get("roles");

    return roles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toSet());
  }
}
