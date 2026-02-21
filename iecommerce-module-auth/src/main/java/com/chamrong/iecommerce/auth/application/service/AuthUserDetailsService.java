package com.chamrong.iecommerce.auth.application.service;

import com.chamrong.iecommerce.auth.domain.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bridges Spring Security's UserDetailsService with the auth domain. */
@Service
public class AuthUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public AuthUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Called by Spring Security's filter chain before tenant context is established. Cross-tenant
   * lookup by username is intentional here — the JWT filter resolves the tenant afterwards.
   */
  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("deprecation") // intentional: no tenant context available at this stage
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    // Flatten all permissions from all roles as GrantedAuthority
    var authorities =
        user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(
                p ->
                    new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        p.getName()))
            .toList();

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(authorities)
        .build();
  }
}
