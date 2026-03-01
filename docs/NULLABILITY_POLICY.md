# Nullability Policy

**Source of truth:** Consistent with SAAS_ENTERPRISE_ARCHITECTURE_SPEC and Sonar-friendly clean code.

## Rules

- **API, application, infrastructure:** Use `@NonNullApi` and `@NonNullFields` at package level via `package-info.java`. No redundant nullability annotations on parameters/returns unless overriding (e.g. `@Nullable` for optional).
- **Domain:** No Spring annotations; use documentation only in package-info. Domain model types use primitives or explicit nullable types where optional (e.g. `Optional`, or document nullability in JavaDoc).
- **Avoid:** Redundant `@NonNull` on every parameter when package is already `@NonNullApi`. Use `@Nullable` only for intentionally nullable parameters/returns.
- **Sonar:** Satisfy null-safety without noise; prefer single package-level contract.
