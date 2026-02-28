package com.chamrong.iecommerce.auth.application.query;

import com.chamrong.iecommerce.auth.domain.IdentityService;
import com.chamrong.iecommerce.auth.domain.idp.SocialProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Query handler that lists social identity providers enabled in the Keycloak realm.
 *
 * <p>Results are cached in {@code social_providers} cache (60-minute TTL) because the provider list
 * is essentially static at runtime — it only changes when admins reconfigure the Keycloak realm.
 *
 * <p>The frontend uses the returned {@link SocialProvider#alias()} as the {@code kc_idp_hint} query
 * parameter when redirecting to Keycloak's auth endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListSocialProvidersHandler {

  private final IdentityService identityService;

  /**
   * Returns all enabled social identity providers.
   *
   * @return cached list of providers; never null, may be empty if no IdPs are configured
   */
  @Cacheable("social_providers")
  public List<SocialProvider> handle() {
    log.debug("Fetching social providers from Keycloak (cache miss)");
    final List<SocialProvider> providers = identityService.listSocialProviders();
    log.info("Found {} social provider(s)", providers.size());
    return providers;
  }
}
