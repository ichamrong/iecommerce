# JPA Entity Guide

**Version:** 1.0  
**Purpose:** Safe Lombok usage, @Version, null-safety, and Sonar-friendly entity patterns.  
**Reference:** SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md; Phase 5 package-info + null safety.

---

## 1. Lombok policy

- **DO:** Use `@Getter` / `@Setter` selectively (e.g. @Getter on entity; @Setter only for mutable fields).
- **DO NOT:** Use `@Data` on entities (generates equals/hashCode on all fields; mutable collections and version cause issues).
- **Optional:** @Builder for DTOs/value objects; for JPA entities use with care (ensure default constructor and field init work with JPA).
- **Equals/hashCode:** Prefer business key (e.g. id) or exclude mutable collections; document if using Lombok-generated.

---

## 2. @Version (optimistic locking)

- **DO:** Add `@Version` on a numeric field (Long or Integer) for entities that need optimistic locking (order, shift, session, etc.).
- **DO NOT:** Make the version field `final` or initialize it to a non-null value that might conflict with DB; let JPA set it.
- **Pattern:** `private Long version;` with getter/setter; no `final`.

---

## 3. Null-safety and initialization

- **@NonNullApi / @NonNullFields:** When used in package-info, all fields are considered non-null unless explicitly @Nullable. JPA entities often have id assigned by DB (null until persisted) — use `@Nullable` on id if needed, or ensure constructor/init does not require id.
- **Required fields:** Prefer constructor initialization for required non-null fields (tenantId, code, etc.); or initialize to default and set in factory method.
- **Collections:** Initialize collections to empty (e.g. `new ArrayList<>()`) to avoid NPE when JPA loads; avoid final collection fields that are not set in constructor.

---

## 4. Domain vs persistence

- **Domain has ZERO Spring/JPA:** Domain model classes used in core logic should not have JPA annotations; persistence entities can live in infrastructure and be mapped to domain models.
- **If entity is in domain:** Some modules keep JPA entities in domain for simplicity; then use @Getter/@Setter and minimal annotations; no Spring in domain except if strictly necessary for JPA (jakarta.persistence is acceptable in domain entity if that’s the chosen design).

---

## 5. Entity template (safe pattern)

```java
@Entity
@Table(name = "example")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExampleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String tenantId;

  @Version
  private Long version;

  @Column(nullable = false)
  private String businessKey;

  public ExampleEntity(String tenantId, String businessKey) {
    this.tenantId = tenantId;
    this.businessKey = businessKey;
  }
}
```

- No @Data; no final on version; required fields set in constructor or via setter before persist.

---

## 6. Common issues and fixes

| Issue | Fix |
|-------|-----|
| "Field 'version' may be 'final'" | Remove final; use Long version with setter |
| "@NonNullFields fields must be initialized" | Constructor init or @Nullable on id where appropriate |
| equals/hashCode on entity with collections | Exclude collections; use id or business key only |
| N+1 on list endpoint | Use projections or fetch join; avoid lazy load in loop |

---

*End of JPA_ENTITY_GUIDE.md*
