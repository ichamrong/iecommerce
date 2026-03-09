package com.chamrong.iecommerce.auth.infrastructure.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for the API.
 *
 * <p>Allows configured frontend origins to call the API from the browser. When using {@code
 * http.cors(Customizer.withDefaults())}, Spring Security looks for a {@link
 * CorsConfigurationSource} bean; without it, cross-origin requests from SPA frontends are blocked.
 *
 * @see org.springframework.security.config.Customizer#withDefaults()
 */
@Configuration
public class CorsConfig {

  private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

  private static final String DEFAULT_ORIGINS = "http://localhost:8082,http://localhost:5173";

  @Value("${app.cors.allowed-origins:" + DEFAULT_ORIGINS + "}")
  private String allowedOriginsConfig;

  private List<String> getAllowedOrigins() {
    return Arrays.stream(allowedOriginsConfig.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final List<String> origins = getAllowedOrigins();
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(
        List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "X-Correlation-ID",
            "X-Request-ID",
            "X-Tenant-Id"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(true);

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    log.info("CORS enabled for origins: {}", origins);
    return source;
  }
}
