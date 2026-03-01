# JPA Entity Guide — iecommerce-api

**Source:** Enterprise architecture spec; clean code and Sonar-friendly rules.

---

## 1) Where JPA Entities Live

- **Only** in `infrastructure/persistence/jpa/` (or module-equivalent).
- **Never** in `domain/model`. Domain models are pure Java (no `@Entity`, no `jakarta.persistence.*`).
- Map between entity and domain in adapters (e.g. `*Mapper.toDomain(entity)`, `toEntity(domain)`).

---

## 2) Lombok on Entities

| Annotation | Use | Avoid |
|------------|-----|--------|
| `@Getter` | Yes, for all readable fields | — |
| `@Setter` | Only when the field must be mutable (e.g. JPA hydration, version) | On every field by default |
| `@Data` | **No** — generates equals/hashCode on all fields (risky with collections and lazy loading) | Everywhere |
| `@NoArgsConstructor` / `@AllArgsConstructor` | Only if required by JPA or mapping | — |
| `@Builder` | Optional for tests/factories; ensure consistent with JPA requirements | — |

Prefer explicit getters/setters for `@Version` and collection fields to avoid accidental mutation.

---

## 3) @Version (Optimistic Locking)

- Use **one** `@Version` field per aggregate root entity (e.g. `Long version` or `long version`).
- **Do not** make it `final` — JPA and mappers need to set it after load/update.
- On conflict, catch `OptimisticLockException` and return **409 Conflict** or retry with clear message.
- Ensure all update paths load the entity (with version), modify, then save; do not set version manually except in tests.

---

## 4) NonNullFields / Initialization

- Avoid `@NonNullFields` on entity classes if it forces initialization of collections to empty and JPA then replaces them (can cause Sonar/Nullability issues).
- Prefer: `private List<OrderItem> items = new ArrayList<>();` for one-to-many so that add/remove in domain logic does not NPE.
- For optional (nullable) columns use `@Column(nullable = true)` and document; do not use `@NonNull` on the field if the DB allows null.

---

## 5) Naming and Structure

- Table name: `@Table(name = "snake_case_table")`.
- Column name: `@Column(name = "snake_case")` when differing from field name.
- Tenant scope: every tenant-scoped entity has `tenant_id` (from `BaseTenantEntity` or explicit column).
- Indexes: keyset pagination index `(tenant_id, created_at DESC, id DESC)` via Liquibase; no index definitions on entity unless necessary.

---

## 6) Checklist for New/Modified Entities

- [ ] Entity class is in `infrastructure/persistence/jpa/`.
- [ ] No `@Data`; use `@Getter` and selective `@Setter`.
- [ ] `@Version` present on aggregate roots; not final.
- [ ] Collections initialized (e.g. `new ArrayList<>()`); no NPE on add.
- [ ] Domain model is separate (no JPA in domain); adapter maps entity ↔ domain.
- [ ] package-info.java present in the package.

---

*End of JPA_ENTITY_GUIDE.md.*
