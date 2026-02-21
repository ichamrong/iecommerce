package com.chamrong.iecommerce.common;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("iecommerce_test")
          .withUsername("test")
          .withPassword("test");

  @BeforeAll
  static void checkDockerAvailable() {
    assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(),
        "Skipping integration test: Docker is not available in this environment.");

    if (!postgres.isRunning()) {
      postgres.start();
    }
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    if (DockerClientFactory.instance().isDockerAvailable()) {
      registry.add("spring.datasource.url", postgres::getJdbcUrl);
      registry.add("spring.datasource.username", postgres::getUsername);
      registry.add("spring.datasource.password", postgres::getPassword);
    }
  }
}
