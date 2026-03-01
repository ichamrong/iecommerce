# Nullability Policy (Strict)

**Status:** Authoritative for all modules. Apply everywhere.  
**Complements:** NULLABILITY_AND_LOMBOK_POLICY.md, JPA_ENTITY_GUIDE.md

---

## 1. Package default behaviour

- **If a package has `package-info.java` with `@NonNullApi`:**
  - Method parameters and return types are **non-null by default**.
  - Adding `@NonNull` on parameters/return types in that package is **redundant** and must be removed, unless one of the "allowed exception cases" (Section 2) applies.
- **If a package has `@NonNullFields`:**
  - Fields are **non-null by default**.
  - Any field that can be null **must** be explicitly marked `@Nullable`.

## 2. When to use @NonNull (allowed exceptions only)

Keep or add `@NonNull` **only** in these cases:

| Case | Description |
|------|-------------|
| **A** | Public API boundary where the package is **not** under `@NonNullApi` (e.g. packages without package-info nullability need explicit annotations for clarity). |
| **B** | Overriding a method where you must match/satisfy a parent contract (e.g. interface method annotated `@NonNull` and you want explicit alignment). |
| **C** | Lombok builder / constructor parameters where package defaults do not apply clearly. |
| **D** | When you intentionally want to override a package default (rare). |
| **E** | For static analysis clarity in mixed-nullability (legacy) packages — temporary; mark with `// TODO remove when package has @NonNullApi`. |

## 3. When to use @Nullable (mandatory)

**Must** add `@Nullable` in these cases:

- **Spring MVC optional inputs:**
  - `@RequestParam(required = false)`
  - `@RequestHeader(required = false)`
  - `@RequestBody(required = false)`
  - Optional query filters for cursor listing (e.g. search, status, from, to).
- **Repository methods** that can return no result:
  - Prefer `Optional<T>` for single-entity return.
  - Use `@Nullable` only if `Optional` is not possible for compatibility.
- **Fields** that are nullable by design (e.g. `shippedAt`, `cancelledAt`).

## 4. Spring MVC controller rule (important)

Inside packages annotated with `@NonNullApi`:

- **Do not** annotate controller parameters with `@NonNull` when Spring already enforces requiredness (e.g. `@PathVariable`, required `@RequestParam`).
- **Prefer** primitive `long` for path variable IDs when the value must always exist; use `Long` only if you truly allow null (rare for `@PathVariable`).
- **Never** return `@NonNull ResponseEntity` in `@NonNullApi` packages (redundant).
- If a parameter can be missing, mark it `@Nullable` and make the Spring parameter optional (e.g. `required = false`).

### Examples

**Don't (redundant under @NonNullApi):**
```java
public @NonNull ResponseEntity<Void> delete(@NonNull @PathVariable Long id)
public @NonNull ResponseEntity<AssetResponse> getById(@NonNull @PathVariable Long id)
```

**Do:**
```java
public ResponseEntity<Void> delete(@PathVariable long id)
public ResponseEntity<AssetResponse> getById(@PathVariable long id)
```

**Optional parameter (keep @Nullable):**
```java
public ResponseEntity<AssetResponse> copyAsset(
    @PathVariable long id,
    @Nullable @RequestParam(required = false) Long targetFolderId)
```

## 5. Services / repositories / entities

- **Services:** In `@NonNullApi` packages, omit `@NonNull` on parameters and return types; use `@Nullable` (or `Optional<T>`) for optional inputs and single-entity lookups that may be absent.
- **Repositories:** Prefer `Optional<T>` for find-by-id; use `@Nullable` only when `Optional` is not feasible.
- **Entities:** Use `@Nullable` on fields that map to nullable columns; do not rely on `@NonNullFields` for JPA-managed fields that the persistence provider sets.

## 6. Safe list access (code smell fixes)

- Replace `list.get(0)` with `list.getFirst()` when the list type supports it (e.g. Java 21+ `List`, or `Deque`).
- **If the list can be empty**, do **not** call `getFirst()` without a guard:
  - Throw a domain exception with a stable error code, or
  - Return `Optional`, or
  - Validate precondition before access (e.g. `if (list.isEmpty()) throw new XxxNotFoundException(...)`).
- Replace chained access like `response.getData().get(0)` with:
  - `response.getData().stream().findFirst().orElseThrow(...)`, or
  - After an explicit non-empty check: `response.getData().getFirst()`.

## 7. PR reviewer checklist

- [ ] No redundant `@NonNull` on parameters/returns in packages with `@NonNullApi`.
- [ ] No `@NonNull ResponseEntity` in controller methods under `@NonNullApi`.
- [ ] Optional controller parameters (e.g. `required = false`) are annotated `@Nullable`.
- [ ] No unsafe `list.get(0)` without non-empty check or `getFirst()`/`findFirst().orElseThrow(...)`.
- [ ] Repository single-entity lookups use `Optional<T>` or explicit `@Nullable` where appropriate.
- [ ] Exceptions use module-specific domain/application exceptions and stable error codes, not raw `RuntimeException` with ad-hoc messages.

---

*End of Nullability Policy.*
