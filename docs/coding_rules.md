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

## 4. Platform Specifics
- **Soft Delete**: Always inherit from `BaseEntity` and use `entity.setDeleted(true)` instead of repository deletes.
- **Tenant Context**: Never hardcode `tenantId`. Always retrieve it from `TenantContext.getCurrentTenant()`.
- **Logging**: Use Structured Logging with slf4j. Every error must include a context or Correlation ID.

## 5. Testing (TDD)
- **Red-Green-Refactor**: No production code is written without a failing test first.
- **Isolation**: Use `Testcontainers` for database-heavy tests and `Mockito` for external service mocks.
