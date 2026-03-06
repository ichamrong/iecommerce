package com.chamrong.iecommerce.auth;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Minimal Spring Boot configuration for tests in the auth module.
 *
 * <p>Provides a local {@code @SpringBootConfiguration} so that Spring Boot test slices such as
 * {@code @WebMvcTest} can bootstrap without depending on the main application module. Component
 * scanning is intentionally omitted so that slice tests control which beans are loaded.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class AuthTestApplication {}
