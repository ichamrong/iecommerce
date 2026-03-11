package com.chamrong.iecommerce.auth.infrastructure.config;

import com.chamrong.iecommerce.auth.infrastructure.security.CookieBearerTokenResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BearerTokenResolverConfig {

  @Bean
  public CookieBearerTokenResolver cookieBearerTokenResolver(
      AuthCookieProperties authCookieProperties) {
    return new CookieBearerTokenResolver(authCookieProperties);
  }
}
