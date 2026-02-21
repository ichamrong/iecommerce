# Coding Rules & Standards

This document defines the core coding principles for the `iecommerce` platform, synthesized from industry-standard literature including *Domain-Driven Design with Java* and *Java Coding Problems*.

## 1. Modular Monolith & DDD Principles
*Based on: Domain-driven design with Java (Premanand Chandrasekaran)*

- **Ubiquitous Language**: Use domain terms in code (e.g., `BookableResource` instead of `ProductItem`).
- **Strict Boundaries**: Modules must interact **only** via public APIs or Published Events. Direct database access across modules is FORBIDDEN.
- **Value Objects**: Prefer Value Objects (e.g., `Money`, `Quantity`) over primitives to prevent "Primitive Obsession".
- **Aggregate Roots**: Ensure data consistency within a module by only accessing entities through their Aggregate Root.

## 2. Java Best Practices
*Based on: Java Coding Problems (Anghel Leonard) & Learning Java*

- **Java 21 Syntax**: Use `var` for local variables when the type is obvious. Use Text Blocks for SQL/JSON.
- **Null Safety**: Use `Optional<T>` for return types that might be empty. Avoid returning `null`.
- **Immutability**: Make classes `final` and fields `private final` by default. Use Java `records` for DTOs and internal data carriers.
- **Stream API**: Use functional programming patterns for collection processing but avoid overly complex one-liners that hurt readability.

## 3. Design Patterns & Clean Code
*Based on: Practical Design Patterns for Java Developers & Head First Java*

- **SOLID Principles**:
    - **S**: One class, one responsibility (e.g., `PaymentService` should not handle `EmailNotification`).
    - **O**: Use interfaces for pluggable strategies (e.g., `SmsProvider`).
    - **D**: Depend on abstractions, not implementations.
- **Fail Fast**: Validate inputs at the beginning of methods using `Assert` or custom exceptions.
- **Meaningful Names**: 
    - Variables: Nouns (e.g., `totalPrice`).
    - Methods: Verbs (e.g., `calculateTotal()`).
    - Boolean: Is/Has (e.g., `isDeleted`).

## 4. Refactoring code configurations
*Based on: Refactoring in Java (Stefano Violetta)*

- **Constants over Absolute Values**: Avoid using raw absolute values (magic numbers or hardcoded strings) directly in the code or tests. Extract them into well-named `public static final` constants.
- **Code Smells**: Continuously refactor code to eliminate duplication and long methods.

## 5. Platform Specifics
- **Soft Delete**: Always inherit from `BaseEntity` and use `entity.setDeleted(true)` instead of repository deletes.
- **Tenant Context**: Never hardcode `tenantId`. Always retrieve it from `TenantContext.getCurrentTenant()`.
- **Logging**: Use Structured Logging with slf4j. Every error must include a context or Correlation ID.

## 6. Testing (TDD)
- **Red-Green-Refactor**: No production code is written without a failing test first.
- **Isolation**: Use `Testcontainers` for database-heavy tests and `Mockito` for external service mocks.

## 7. Import & Package Rules

### 7.1 No Wildcard Imports
**Never** use wildcard imports (`*`). Every import must be explicit.

```java
// ❌ FORBIDDEN
import org.springframework.web.bind.annotation.*;
import java.util.*;

// ✅ CORRECT
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Optional;
```

**Rationale:** Wildcard imports hide what is actually being used, cause naming conflicts,
and make code reviews harder. IntelliJ enforces this via **Settings → Editor → Code Style → Java → Imports → Class count to use import with '*' = 999**.

---

### 7.2 Every Package Must Have a `package-info.java`
Each package must contain a `package-info.java` file with a short Javadoc description
of the package's responsibility.

```java
/**
 * Application layer for the authentication module.
 *
 * <p>Contains command handlers, query handlers, and DTOs.
 * This layer orchestrates domain logic and delegates to the domain model.
 */
package com.chamrong.iecommerce.auth.application;
```

**Required packages** to document:
- `domain` — core business rules and entities
- `application` — use cases, commands, queries, DTOs
- `infrastructure` — persistence, security, external integrations
- `api` — REST controllers and request/response models

**Rationale:** Acts as living documentation. Anyone entering a new package immediately
understands its purpose without reading multiple files.

## 8. SonarQube & Code Quality
*Common issues and rules to maintain high quality scores.*

- **JUnit 5 Visibility**: Don't use `public` on test classes or test methods. JUnit 5 prefers package-private (default) visibility.
- **Loggers over standard output**: Never use `System.out.println()` or `System.err.println()`. Use `log.info()`, `log.error()`, etc., via SLF4J.
- **Generic Exceptions**: Avoid throwing or catching `java.lang.Exception`, `java.lang.RuntimeException`, or `java.lang.Throwable`. Use specific checked or unchecked exceptions.
- **Cognitive Complexity**: Keep methods simple. If a method's cognitive complexity exceeds 15 (due to nested loops, if-statements, etc.), it must be refactored.
- **Dead Stores**: Remove any local variables that are assigned but never read.
- **Null-Safe Equals**: When comparing a variable to a constant string, use `"CONSTANT".equals(variable)` to prevent `NullPointerException`.
- **String Concatenation in Loops**: Avoid `s += "next"` in loops. Use `StringBuilder` for better performance.
- **Modern Collection Factory Methods**: Use `List.of()`, `Set.of()`, and `Map.of()` instead of `Arrays.asList()` when an immutable collection is needed.
- **Utility Class Private Constructor**: Classes that only contain static methods (utility classes) must have a `private` constructor to prevent instantiation.
- **Constants for Magic Literals**: Strings and numbers used more than once (or even once if they have specific meaning) should be extracted to `static final` constants.
- **Dependency Injection**: Prefer **Constructor Injection** over field injection (`@Autowired`). It makes the code easier to test and ensures required dependencies are not null.
- **Method References**: Use method references (e.g., `String::isEmpty`) instead of lambdas when possible (e.g., `s -> s.isEmpty()`).
- **Collection Returns**: Always return an empty collection (e.g., `Collections.emptyList()`) instead of `null` to prevent downstream NPEs.
- **Naming Constants**: Constants (static final) must be in `UPPER_SNAKE_CASE`.
- **Naming Booleans**: Use prefixes like `is`, `has`, `can`, or `should` for boolean variables and methods (e.g., `isActive`, `hasPermission`).
- **Bean Validation**: Use Jakarta Validation annotations (e.g., `@NotNull`, `@NotBlank`, `@Size`) in DTOs instead of manual validation logic in controllers or services.
- **Avoid Raw Types**: Never use raw types like `List` or `Map`. Always use generics: `List<String>`.
- **Try-with-Resources**: Use `try (...) { ... }` blocks for any objects that implement `AutoCloseable` (e.g., Streams, DB connections) to ensure they are closed properly.
- **Java Time API**: Use `java.time.LocalDate`, `LocalDateTime`, and `Instant`. Never use `java.util.Date` or `java.util.Calendar`.
- **Override Annotation**: Always use `@Override` when implementing an interface method or overriding a parent method to catch signature mismatches at compile time.

## 9. Lombok Best Practices
*Guidelines for using Lombok effectively while avoiding common pitfalls.*

- **Constructor Injection**: Use `@RequiredArgsConstructor` on classes (Services, Controllers) with `private final` fields. This is the cleanest way to perform dependency injection.
- **Avoid `@Data` on JPA Entities**: `@Data` generates `equals()`, `hashCode()`, and `toString()` which can trigger lazy-loading of collections or cause infinite recursion in circular relationships. Prefer `@Getter` and `@Setter` at the class or field level.
- **Explicit Equals/HashCode for Entities**: For entities, manually implement `equals()` and `hashCode()` using the business key (e.g., a UUID or unique natural key), or use `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` with `@EqualsAndHashCode.Include` on specific fields.
- **Consistent Logging**: Use `@Slf4j` instead of manually declaring loggers.
- **Exclude Sensitive Data**: Always use `@ToString(exclude = {"password", "secret"})` on classes that hold sensitive information to prevent accidental logging.
- **Builders**: Use `@Builder` for complex object creation to improve readability. When used on a class, ensure a `@AllArgsConstructor` (private) exists.
- **Lombok vs Records**: Do not use Lombok annotations on Java `records`. Records already provide getters, constructors, equals, hashCode, and toString by design.
- **Cleanup**: Use `@Cleanup` for local variables that need closing only if they don't support try-with-resources (though try-with-resources is generally preferred).

