# Folder Structure Standard — Mandatory for All Modules

**Source of truth:** `docs/SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md` Section 2.5.

Every module under `iecommerce-api` MUST follow this structure. No exceptions.

---

## 1) Exact Tree

```
<module-root>/src/main/java/com/chamrong/iecommerce/<module>/
├── api
│   ├── package-info.java
│   ├── *Controller.java
│   └── *Api.java (optional facade interface)
├── application
│   ├── package-info.java
│   ├── command
│   │   ├── package-info.java
│   │   ├── *Command.java
│   │   ├── *Handler.java
│   │   └── *Validator.java (optional)
│   ├── query
│   │   ├── package-info.java
│   │   ├── *Query.java (optional)
│   │   ├── *QueryService.java
│   │   └── *Projection.java
│   ├── usecase
│   │   ├── package-info.java
│   │   └── *UseCase.java
│   └── dto
│       ├── package-info.java
│       ├── *Request.java
│       └── *Response.java
├── domain
│   ├── package-info.java
│   ├── model
│   │   ├── package-info.java
│   │   └── (pure domain objects; NO Spring, NO jakarta.persistence)
│   ├── event
│   │   ├── package-info.java
│   │   └── *Event.java
│   ├── ports
│   │   ├── package-info.java
│   │   ├── *RepositoryPort.java
│   │   ├── *ClientPort.java
│   │   ├── *PublisherPort.java
│   │   └── *IdempotencyPort.java
│   ├── policy
│   │   ├── package-info.java
│   │   └── *Policy.java
│   ├── service
│   │   ├── package-info.java
│   │   └── *DomainService.java
│   └── exception
│       ├── package-info.java
│       └── *DomainException.java
└── infrastructure
    ├── package-info.java
    ├── config
    │   ├── package-info.java
    │   └── *Configuration.java
    ├── persistence
    │   ├── package-info.java
    │   └── jpa
    │       ├── package-info.java
    │       ├── *Entity.java
    │       ├── SpringData*Repository.java
    │       ├── Jpa*Adapter.java (implements ports)
    │       ├── *Mapper.java
    │       └── *Specification.java
    ├── outbox
    │   ├── package-info.java
    │   ├── *OutboxEventEntity.java
    │   ├── Jpa*OutboxRepository.java
    │   ├── *OutboxPublisher.java
    │   └── *OutboxRelayScheduler.java
    ├── saga
    │   ├── package-info.java
    │   ├── *SagaStateEntity.java
    │   ├── *SagaOrchestrator.java
    │   ├── *SagaListener.java
    │   └── *CompensationHandler.java
    └── client
        ├── package-info.java
        ├── *ClientAdapter.java
        └── providers/ (optional)
```

---

## 2) Dependency Rules

| Layer | Allowed | Forbidden |
|-------|---------|-----------|
| **Domain** | Pure Java; interfaces in `ports` | Spring; jakarta.persistence; repository interfaces in domain root or `domain/repository`, `domain/port` (singular) |
| **Application** | Spring @Service, @Transactional; calls domain + ports | Business logic in controllers; direct JPA |
| **Infrastructure** | Implements ports; JPA entities; adapters; outbox; saga; client | Leaking JPA entities to API/application |
| **API** | Controllers; validation; mapping to DTO/Command | Business logic |

---

## 3) Do / Don’t (from this repo)

### Do
- Put **repository interfaces** only in `domain/ports` (e.g. `OrderRepositoryPort`, `InvoiceRepositoryPort`, `AuditRepositoryPort`).
- Name ports with suffix `*Port`; adapters with `*Adapter` (e.g. `JpaOrderAdapter`, `StripePaymentAdapter`).
- Keep **JPA entities** only in `infrastructure/persistence/jpa`.
- Map entities to domain models in adapters; return domain objects from ports.
- Add **package-info.java** in every package; use `@NonNullApi` / `@NonNullFields` in api, application, infrastructure; domain packages documentation only.

### Don’t
- **Do not** create `domain/repository` or `domain/port` (singular). Use `domain/ports` only.
- **Do not** put repository interfaces in domain root (e.g. `domain/OrderRepository.java`).
- **Do not** put JPA entities or `@Entity` in `domain/model`.
- **Do not** use `@Data` on JPA entities; prefer `@Getter` and `@Setter` only when needed.
- **Do not** use offset pagination for list endpoints; use cursor (CursorCodec + FilterHasher + CursorPageResponse).

---

## 4) Naming Conventions

| Kind | Convention | Example |
|------|------------|---------|
| Port (interface) | *Port | OrderRepositoryPort, PaymentProviderPort |
| Adapter (impl) | *Adapter | JpaOrderAdapter, StripeAdapter |
| Command | *Command | OpenShiftCommand |
| Handler | *Handler | OpenShiftHandler |
| Query service | *QueryService | SaleQueryService |
| Domain exception | *DomainException | InvoiceImmutableException |

---

## 5) Reviewer Checklist

- [ ] No `domain/repository` or `domain/port` (singular); only `domain/ports`.
- [ ] No repository interface in domain root.
- [ ] No Spring or jakarta.persistence in domain/model and domain/event.
- [ ] JPA entities only in infrastructure/persistence/jpa.
- [ ] Every package has package-info.java.
- [ ] List endpoints use cursor pagination (no offset); CursorPageResponse; filterHash validation.

---

*End of FOLDER_STRUCTURE_STANDARD.md.*
