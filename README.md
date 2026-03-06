## iecommerce-api local setup

- **Prerequisites**: Docker & Docker Compose, Java 21+, Maven 3.9+.
- **Start infrastructure**:
  - `cd iecommerce-api/deployment`
  - `docker compose up -d`
  - Services: Postgres (5432), Keycloak (8080), Redis (6379), MinIO (9000/9001), Kong (8000/8001), Vault (8200), Loki (3100), Grafana (3000), SonarQube (9002), RabbitMQ (5672/15672), MailHog (1025/8025).
- **Run application**:
  - `cd iecommerce-api`
  - `POSTGRES_PASSWORD=admin mvn -pl iecommerce-app spring-boot:run`
- **Access**:
  - API: `http://localhost:8081`
  - Swagger UI: `http://localhost:8081/swagger-ui.html`
  - Keycloak admin: `http://localhost:8080`
  - MailHog UI: `http://localhost:8025`
  - Grafana: `http://localhost:3000`
  - SonarQube: `http://localhost:9002`