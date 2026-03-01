package com.chamrong.iecommerce.sale;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Minimal configuration for sale cursor pagination integration tests. No component scan so only JPA
 * entities and repositories are loaded.
 */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.chamrong.iecommerce.sale.infrastructure.persistence.jpa")
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.chamrong.iecommerce.sale.infrastructure.persistence.jpa")
public class SaleCursorPaginationTestApplication {}
