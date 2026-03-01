# Domain JPA Migration — Order & Auth

**Date:** 2025-03-01  
**Reference:** AUDIT_REMEDIATION_PLAN.md (Remaining Medium/Low), AUDIT_ENTERPRISE_REPORT.md, FOLDER_STRUCTURE_STANDARD.md, SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md

**Status:** Order aggregate and Auth Tenant migration **completed.** Auth User, Role, Permission, PosTerminal, PosSession, Group remain as JPA in domain for a future phase.

---

## 1. Current state (pre-migration; Order and Tenant now done)

Per SAAS_ENTERPRISE_ARCHITECTURE_SPEC and FOLDER_STRUCTURE_STANDARD:

- **Domain** must have **zero** Spring or `jakarta.persistence` on domain model classes used in core logic; persistence entities belong in infrastructure and are mapped to domain models.

### 1.1 Order module

| Location | Classes | Issue |
|----------|---------|--------|
| `order/domain/` | `Order.java`, `OrderItem.java`, `OrderAuditLog.java`, `OrderOutboxEvent.java`, `OrderIdempotency.java` | `@Entity`, `@Table`, `jakarta.persistence.*` in domain |
| `order/domain/saga/` | `OrderSagaState.java` | JPA entity in domain |

**Ports:** Already in `domain/ports/` (OrderRepositoryPort, OrderAuditPort, OrderOutboxPort, OrderIdempotencyPort, OrderSagaStatePort, ClockPort). Implementations live in infrastructure.

### 1.2 Auth module

| Location | Classes | Issue |
|----------|---------|--------|
| `auth/domain/` | `Tenant.java`, `User.java`, `Role.java`, `Permission.java`, `PosTerminal.java`, `PosSession.java`, `Group.java` | JPA entities in domain (used by TenantContextFilter, handlers, and repositories) |

**Ports:** Already in `domain/ports/` (TenantRepositoryPort, UserRepositoryPort, RoleRepositoryPort, PermissionRepositoryPort, PosTerminalRepositoryPort, PosSessionRepositoryPort). Implementations in infrastructure.

---

## 2. Target state

- **Domain:** Pure aggregates / value objects / domain events only. No `@Entity`, no `jakarta.persistence`, no Spring.
- **Infrastructure:** `infrastructure/persistence/jpa/` (and subpackages such as `entity/`, `repository/`):
  - JPA entity classes (e.g. `OrderEntity`, `OrderItemEntity`, `TenantEntity`, `UserEntity`).
  - Spring Data repository interfaces and JPA adapter implementations.
  - Mappers: entity ↔ domain model (used by adapters when loading/saving).

**Reference pattern:** Invoice module (after remediation): JPA adapters and entities under `persistence/jpa`; domain has no JPA.

---

## 3. Migration steps (recommended order)

### Order module

1. Introduce `order/domain/model/` (or keep current package) with **domain** aggregates: `Order`, `OrderItem`, etc. — no JPA annotations; use Lombok/value types as needed.
2. Create `order/infrastructure/persistence/jpa/entity/`: `OrderEntity`, `OrderItemEntity`, `OrderAuditLogEntity`, `OrderOutboxEventEntity`, `OrderIdempotencyEntity`, `OrderSagaStateEntity` — move JPA from domain classes.
3. Add mappers in infrastructure (e.g. `OrderPersistenceMapper`) to convert entity ↔ domain aggregate.
4. Update JPA adapters to load entity, map to domain, return domain; on save, map domain → entity and persist.
5. Update all domain logic to use domain models only; remove JPA from `order/domain/**` and `order/domain/saga/**`.
6. Run full test suite and order-related integration tests.

### Auth module

1. Introduce `auth/domain/model/` with **domain** types: `Tenant`, `User`, `Role`, etc. — no JPA.
2. Create `auth/infrastructure/persistence/jpa/entity/`: `TenantEntity`, `UserEntity`, `RoleEntity`, etc. — move JPA from domain.
3. Add mappers entity ↔ domain in infrastructure.
4. Update `JpaTenantRepository`, `JpaUserRepositoryAdapter`, etc., to use entities and mappers; expose domain types via ports.
5. Update TenantContextFilter, handlers, and any code using `Tenant`/`User` to use domain models (loaded via ports).
6. Remove JPA from `auth/domain/**`; run full test suite and auth-related tests.

---

## 4. Schedule and completion

| Phase | Scope | Status |
|-------|--------|--------|
| **Order domain JPA migration** | Order + OrderItem pure domain; OrderEntity/OrderItemEntity + OrderPersistenceMapper; JpaOrderAdapter | **DONE** (2025-03-01) |
| **Auth Tenant JPA migration** | Tenant + TenantPreferences pure domain; TenantEntity + TenantPreferencesEmbeddable + TenantPersistenceMapper; JpaTenantAdapter | **DONE** (2025-03-01) |
| **Auth User, Role, Permission, PosTerminal, PosSession, Group** | Same pattern as Tenant | Pending (follow-up phase) |

**Done:** common has BaseDomainEntity and BaseDomainTenantEntity (no JPA). Order and Auth Tenant migrations completed; optional follow-up for remaining auth entities.

---

## 5. Out of scope for this migration

- **Internal batch processing** (outbox relay, findPending) using `PageRequest.of(0, batchSize)`: limit-only, not user-facing; acceptable per spec focus on list endpoints. No change required.
- **package-info completion:** Already DONE for audit and payment subpackages; other modules incremental as needed.

---

*This document satisfies the “document and schedule” requirement in AUDIT_REMEDIATION_PLAN.md Remaining Medium/Low.*
