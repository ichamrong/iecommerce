# Nullability and Lombok Policy — iecommerce-api

**Applies to:** All modules; api, application, infrastructure, and domain packages.

---

## 1) Package-Level Nullability

- **api, application, infrastructure:** Use `@NonNullApi` and `@NonNullFields` in `package-info.java` so that method parameters and return types are non-null by default; use `@Nullable` for optional values.
- **domain:** Documentation only in package-info (no Spring nullability annotations) so domain stays framework-free.

Example `package-info.java` (api/application/infrastructure):

```java
@org.springframework.lang.NonNullApi
@org.springframework.lang.NonNullFields
package com.chamrong.iecommerce.<module>.api;
```

---

## 2) Lombok and Null Safety

- **@Getter / @Setter:** Safe; use where appropriate.
- **@Data:** Do not use on JPA entities (equals/hashCode/toString on all fields can break lazy loading and collections).
- **@Builder:** Safe for DTOs and commands; ensure default values for optional fields are explicit.
- **@RequiredArgsConstructor:** Safe when used with `final` fields; avoid on entities with circular or lazy references if it confuses Sonar.
- **Optional:** Use `Optional<T>` for return types that can be absent (e.g. `findById`); do not use Optional as a field type in entities.

---

## 3) Entity and Domain Model Fields

- **JPA entities:** Initialize collections (e.g. `private List<Item> items = new ArrayList<>();`) to avoid NPE when adding in domain logic.
- **Version field:** Must be mutable (not final); JPA sets it after persist/merge.
- **Nullable columns:** Use `@Column(nullable = true)` and document; mark getter or parameter as `@Nullable` if in application layer.

---

## 4) Logging and PII

- Do not log PII (emails, payment details, full names) in plain form; use `@Masked` or truncate in log messages.
- Use correlationId and tenantId in MDC for traceability; no sensitive data in log format strings.

---

## 5) Constants

- Replace magic strings with constants (e.g. error codes: `INVALID_CURSOR_FILTER_MISMATCH`, `TENANT_SUSPENDED`).
- Use enums for fixed sets (status, type) in domain and API.

---

*End of NULLABILITY_AND_LOMBOK_POLICY.md.*
