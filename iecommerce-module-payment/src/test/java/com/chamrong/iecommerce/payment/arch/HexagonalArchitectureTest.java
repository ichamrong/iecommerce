package com.chamrong.iecommerce.payment.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces Hexagonal (Ports & Adapters) architecture boundaries for the payment module.
 *
 * <p>These tests run in CI and will FAIL if any new class violates the dependency rules. All new
 * code must respect the boundaries. Legacy violations are explicitly excluded below and tracked as
 * Phase 2 migration targets (see enterprise_architecture.md).
 *
 * <h2>Legacy exclusions (Phase 2 migration targets):</h2>
 *
 * <ul>
 *   <li>{@code Payment} — has @Entity; target: extract PaymentEntity to infrastructure
 *   <li>{@code FinancialLedger} — has @Entity; already extracted to FinancialLedgerEntity
 *   <li>{@code PaymentOutboxEvent} — has @Entity; target: move to infrastructure/outbox
 *   <li>Outbox: domain uses {@code PaymentOutboxPort}; infrastructure implements via
 *       JpaPaymentOutboxAdapter + SpringDataPaymentOutboxRepository
 *   <li>{@code FinancialLedgerRepository} — extends JpaRepository; replaced by
 *       SpringDataFinancialLedgerRepository
 * </ul>
 *
 * <h2>Allowed dependency graph:</h2>
 *
 * <pre>
 *   api → application (allowed)
 *   application → domain (allowed)
 *   infrastructure → domain (allowed — implements ports)
 *   domain → domain (self only)
 *   ─────── FORBIDDEN ───────
 *   domain → Spring, JPA, infrastructure
 *   api → infrastructure
 *   application → infrastructure
 * </pre>
 */
@AnalyzeClasses(packages = "com.chamrong.iecommerce.payment")
public class HexagonalArchitectureTest {

  // ── Rule 1: Domain must have no Spring annotations ─────────────────────────

  @ArchTest
  static final ArchRule domainMustNotDependOnSpring =
      noClasses()
          .that()
          .resideInAPackage("..payment.domain..")
          // package-info.java uses @NonNullApi and @NamedInterface — Spring Modulith metadata, not
          // logic:
          .and()
          .doNotHaveSimpleName("package-info")
          // LEGACY exclusions — track for Phase 2 migration:
          .and()
          .doNotBelongToAnyOf(
              com.chamrong.iecommerce.payment.domain.FinancialLedgerRepository.class)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "org.springframework.stereotype..",
              "org.springframework.beans..")
          .because("Domain must be pure Java — no Spring annotations allowed");

  // ── Rule 2: Domain must have no JPA annotations ────────────────────────────

  @ArchTest
  static final ArchRule domainMustNotDependOnJpa =
      noClasses()
          .that()
          .resideInAPackage("..payment.domain..")
          // LEGACY exclusions — track for Phase 2 migration:
          .and()
          .doNotBelongToAnyOf(
              com.chamrong.iecommerce.payment.domain.Payment.class,
              com.chamrong.iecommerce.payment.domain.FinancialLedger.class,
              com.chamrong.iecommerce.payment.domain.PaymentOutboxEvent.class,
              com.chamrong.iecommerce.payment.domain.FinancialLedgerRepository.class)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("jakarta.persistence..", "javax.persistence..", "org.hibernate..")
          .because("Domain must be pure Java — no JPA/Hibernate annotations allowed");

  // ── Rule 3: Domain must not depend on infrastructure ──────────────────────

  @ArchTest
  static final ArchRule domainMustNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..payment.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..payment.infrastructure..")
          .because("Domain must NOT depend on infrastructure — use ports instead");

  // ── Rule 4: API layer must not touch infrastructure ───────────────────────

  @ArchTest
  static final ArchRule apiMustNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..payment.api..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..payment.infrastructure..")
          .because("API/web adapters must only talk to application use cases, not infrastructure");

  // ── Rule 5: Application layer must not touch infrastructure ───────────────

  @ArchTest
  static final ArchRule applicationMustNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..payment.application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..payment.infrastructure..")
          .because(
              "Application use cases must depend on domain ports, not infrastructure adapters");

  // ── Rule 6: Events must not depend on Spring or infrastructure ────────────

  @ArchTest
  static final ArchRule eventsMustBePureJava =
      noClasses()
          .that()
          .resideInAPackage("..payment.event..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..payment.infrastructure..", "org.springframework..", "jakarta.persistence..")
          .because("Domain events must be serializable plain records/classes");

  // ── Rule 7: Application must not use JDBC/JPA directly ───────────────────

  @ArchTest
  static final ArchRule applicationMustNotImportJdbc =
      noClasses()
          .that()
          .resideInAPackage("..payment.application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("java.sql..", "javax.sql..", "jakarta.persistence..")
          .because("Application layer must not know about SQL or JPA");
}
