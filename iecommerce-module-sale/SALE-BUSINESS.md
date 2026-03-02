## Sale Module – Business Workflows & Architecture Map

### 1. Scope and Responsibilities

The sale module owns **front-of-house sales flows** for the platform:

- **Shifts**: staff work periods on a terminal.
- **Sale sessions**: cashier sessions within a shift, tracking expected vs actual cash.
- **Quotations**: pre-order offers (draft → sent → confirmed/canceled).
- **Returns**: post-sale refunds tied to original orders.
- **Cross-module orchestration**: a saga from confirmed quotation → order creation → stock reservation → payment initiation.

Technical responsibilities are implemented with a DDD/hexagonal layout:

- `api`: HTTP endpoints.
- `application`: use cases, commands, queries, saga orchestration.
- `domain`: aggregates, ports, policies, domain events.
- `infrastructure`: JPA mappings, Spring Data repositories, outbox, external adapters.

---

### 2. APIs and Their Use Cases

All controllers live under:

- `iecommerce-api/iecommerce-module-sale/src/main/java/com/chamrong/iecommerce/sale/api`

#### 2.1 Shifts & Sessions – `SaleController`

Base path: `/api/v1/sales`

- **Open shift**
  - **Endpoint**: `POST /api/v1/sales/shifts`
  - **Controller**: `SaleController.openShift`
  - **Use case**: `ShiftUseCase.openShift(OpenShiftCommand)`
  - **Domain aggregate**: `Shift`
  - **Business flow**:
    - Enforces that a staff member does not already have an active shift on the same terminal.
    - Creates a new `Shift(tenantId, staffId, terminalId)` and persists via `ShiftRepositoryPort`.
    - Returns `ShiftResponse` with basic shift metadata and status.

- **List shifts**
  - **Endpoint**: `GET /api/v1/sales/shifts`
  - **Controller**: `SaleController.listShifts`
  - **Use case**: `SaleQueryService.listShifts`
  - **Domain**: reads shift projections via repository ports.
  - **Business flow**:
    - Uses `TenantContext.requireTenantId()` and cursor-based pagination.
    - Returns a `CursorPageResponse<ShiftResponse>` for reporting and back-office UIs.

- **Close shift**
  - **Endpoint**: `PATCH /api/v1/sales/shifts/{id}/close`
  - **Controller**: `SaleController.closeShift`
  - **Use case**: `ShiftUseCase.closeShift`
  - **Domain aggregate**: `Shift`
  - **Business flow**:
    - Loads a shift by id + tenant via `ShiftRepositoryPort`.
    - Verifies tenant using `TenantGuard`.
    - Calls `shift.close()` to transition status and persists the result.

- **Open session**
  - **Endpoint**: `POST /api/v1/sales/sessions`
  - **Controller**: `SaleController.openSession`
  - **Use case**: `SaleSessionUseCase.openSession`
  - **Domain aggregate**: `SaleSession` (and `Shift`)
  - **Business flow**:
    - Validates that there is no active session for the given terminal (`findActiveSessionByTerminal`).
    - Loads the owning `Shift` and checks tenant.
    - Creates `SaleSession(shift, tenantId, terminalId, currency)`, persists it, and writes an audit log.
    - `SaleQueryService.toSessionResponse` maps to `SaleSessionResponse`.

- **List sessions**
  - **Endpoint**: `GET /api/v1/sales/sessions`
  - **Controller**: `SaleController.listSessions`
  - **Use case**: `SaleQueryService.listSessions`
  - **Domain**: reads session projections via `SaleSessionRepositoryPort`.
  - **Business flow**:
    - Filters by optional `terminalId`, applies tenant and cursor pagination.
    - Used for back-office and operational monitoring.

- **Initiate closing session**
  - **Endpoint**: `PATCH /api/v1/sales/sessions/{id}/initiate-closing`
  - **Controller**: `SaleController.initiateClosing`
  - **Use case**: `SaleSessionUseCase.initiateClosing`
  - **Domain aggregate**: `SaleSession`
  - **Business flow**:
    - Loads the session by id + tenant, checks tenant.
    - Calls `session.initiateClosing()` (state transition).
    - Persists and logs an audit entry.

- **Close session**
  - **Endpoint**: `PATCH /api/v1/sales/sessions/{id}/close`
  - **Controller**: `SaleController.closeSession`
  - **Use case**: `SaleSessionUseCase.closeSession`
  - **Domain aggregate**: `SaleSession`
  - **Business flow**:
    - Loads session by id + tenant, checks tenant.
    - Calls `session.close(actualCash)` with a `Money` value (expected vs actual cash reconciliation).
    - Persists and logs an audit entry.
    - Returns updated `SaleSessionResponse`.

#### 2.2 Quotations – `QuotationController`

Base path: `/api/v1/sales/quotations`

- **Create quotation**
  - **Endpoint**: `POST /api/v1/sales/quotations`
  - **Controller**: `QuotationController.createQuotation`
  - **Command DTO**: `CreateQuotationCommand`
  - **Use case**: `QuotationUseCase.createQuotation`
  - **Domain aggregate**: `Quotation` + `QuotationItem`
  - **Business flow**:
    - Applies per-tenant rate limiting via `RateLimiter`.
    - Uses `IdempotencyService` with optional `Idempotency-Key` header to enforce idempotent creation.
    - Builds a `Quotation` with items (product, quantity, unit price) and calculates totals via the aggregate.
    - Writes an audit log and persists via `QuotationRepositoryPort`.
    - Returns `QuotationResponse` with items and status.

- **List quotations**
  - **Endpoint**: `GET /api/v1/sales/quotations`
  - **Controller**: `QuotationController.listQuotations`
  - **Use case**: `SaleQueryService.listQuotations`
  - **Domain**: reads via `QuotationRepositoryPort` and pagination helpers.
  - **Business flow**:
    - Tenant-aware, cursor-based listing for sales pipeline and reporting.

- **Confirm quotation**
  - **Endpoint**: `PATCH /api/v1/sales/quotations/{id}/confirm`
  - **Controller**: `QuotationController.confirmQuotation`
  - **Use case**: `QuotationUseCase.confirmQuotation`
  - **Domain aggregate**: `Quotation`
  - **Business flow**:
    - Uses idempotency for repeated confirmation requests.
    - Loads quotation by id + tenant and checks tenant.
    - Calls `quotation.confirm()` to transition status.
    - Persists, writes an audit log, and publishes a `QuotationConfirmedEvent` to the sale outbox via `OutboxPublisher`.
    - This event drives the cross-module saga (see Section 3).

- **Cancel quotation**
  - **Endpoint**: `PATCH /api/v1/sales/quotations/{id}/cancel`
  - **Controller**: `QuotationController.cancelQuotation`
  - **Use case**: `QuotationUseCase.cancelQuotation`
  - **Domain aggregate**: `Quotation`
  - **Business flow**:
    - Loads quotation, checks tenant, calls `quotation.cancel()`, persists, and returns `QuotationResponse`.

#### 2.3 Returns – `ReturnController`

Base path: `/api/v1/sales/returns`

- **Create return**
  - **Endpoint**: `POST /api/v1/sales/returns`
  - **Controller**: `ReturnController.createReturn`
  - **Command DTO**: `CreateReturnCommand`
  - **Use case**: `ReturnUseCase.createReturn`
  - **Domain aggregate**: `SaleReturn` + `ReturnItem`
  - **Business flow**:
    - Enforces idempotency per `(tenantId, returnKey)` at the repository level.
    - Verifies the original order exists via `OrderPort.exists`.
    - For each return line:
      - Loads original order item via `OrderPort.getOrderItem`.
      - Enforces anti-fraud rule: returned quantity cannot exceed purchased quantity.
      - Adds return items with `Money(refundPrice, currency)`.
    - Persists the `SaleReturn` aggregate and returns `SaleReturnResponse`.

- **List returns**
  - **Endpoint**: `GET /api/v1/sales/returns`
  - **Controller**: `ReturnController.listReturns`
  - **Use case**: `SaleQueryService.listReturns`
  - **Domain**: reads via `SaleReturnRepositoryPort` and pagination helpers.

- **Approve return**
  - **Endpoint**: `PATCH /api/v1/sales/returns/{id}/approve`
  - **Controller**: `ReturnController.approveReturn`
  - **Use case**: `ReturnUseCase.approveReturn`
  - **Domain aggregate**: `SaleReturn`
  - **Business flow**:
    - Loads return by id + tenant.
    - Calls `saleReturn.approve(approverId)` to record approval actor and update status.

- **Complete return**
  - **Endpoint**: `PATCH /api/v1/sales/returns/{id}/complete`
  - **Controller**: `ReturnController.completeReturn`
  - **Use case**: `ReturnUseCase.completeReturn`
  - **Domain aggregate**: `SaleReturn`
  - **Business flow**:
    - Moves the return to completed state (refund has been executed).
    - Persists and returns updated `SaleReturnResponse`.

---

### 3. Domain Aggregates and States

All aggregates live under:

- `iecommerce-api/iecommerce-module-sale/src/main/java/com/chamrong/iecommerce/sale/domain/model`

- **`Shift`**
  - Represents a staff member’s shift on a terminal.
  - Key fields: `tenantId`, `staffId`, `terminalId`, `startTime`, `endTime`, `status`.
  - States: typically `OPEN`, `CLOSED` (plus any intermediate states as defined in `ShiftStatus`).
  - Invariants:
    - One active shift per `(tenant, staff, terminal)` enforced by `ShiftUseCase` and `ShiftRepositoryPort`.

- **`SaleSession`**
  - Represents a cashier session within a shift on a specific terminal.
  - Key fields: `tenantId`, `terminalId`, `currency`, `expectedAmount`, `actualAmount`, `status`.
  - States: e.g. `OPEN`, `CLOSING`, `CLOSED` (see `SaleSessionStatus`).
  - Invariants:
    - Only one active session per terminal at a time.
    - State transitions must follow the defined state machine (`open → closing → closed`).

- **`Quotation`**
  - Represents a sales offer before an order is placed.
  - Key fields: `tenantId`, `customerId`, `currency`, `expiryDate`, `status`, `items`, `totalAmount`.
  - States: `DRAFT`, `SENT`, `CONFIRMED`, `CANCELLED`, `EXPIRED` (exact enum values in `QuotationStatus`).
  - Invariants:
    - Quotations must have at least one item before confirmation.
    - Total amount is an aggregate of item totals (unit price × quantity).

- **`SaleReturn`**
  - Represents a return against an original order.
  - Key fields: `tenantId`, `originalOrderId`, `returnKey`, `reason`, `currency`, `status`, `items`, `totalRefundAmount`.
  - States: `REQUESTED`, `APPROVED`, `REJECTED`, `COMPLETED` (per `SaleReturnStatus`).
  - Invariants:
    - Cannot approve or complete a return that is not in the appropriate prior state.
    - Per-line returned quantity cannot exceed original purchase quantity.

---

### 4. Cross-Module Saga: From Quotation to Order/Inventory/Payment

When a quotation is confirmed, the sale module orchestrates a saga that touches order, inventory, and payment modules.

Key elements:

- **Domain event**: `QuotationConfirmedEvent` in `sale.domain.event`.
- **Outbox**: `OutboxPublisher` persists events to `SaleOutboxEvent` via JPA.
- **Relay**: `SaleOutboxRelay` reads pending outbox rows and publishes events to Spring’s `ApplicationEventPublisher`.
- **Saga orchestrator**: `SaleSagaOrchestrator` in `sale.application.saga`.

High-level saga steps:

1. **Quotation confirmed**
   - `QuotationUseCase.confirmQuotation` publishes a `QuotationConfirmedEvent` to the sale outbox.
2. **Outbox relay**
   - `SaleOutboxRelay` deserializes the event and publishes it as an application event.
3. **Saga orchestrator**
   - `SaleSagaOrchestrator.handleQuotationConfirmed` reacts to the event:
     - Creates a sales order via `OrderPort` (integration with `iecommerce-module-order`).
     - Reserves stock via `InventoryPort` (integration with inventory/catalog).
     - Initiates payment via `PaymentPort` (integration with `iecommerce-module-payment`).
   - Maintains progress in `SaleSagaStateEntity` using `JpaSaleSagaRepository`, so compensations can be implemented.

---

### 5. Summary for Business Stakeholders

- The **sale module** models how staff work (shifts), how cashiers operate on terminals (sessions), how offers are presented (quotations), and how customers can return goods (returns).\n- REST APIs in `SaleController`, `QuotationController`, and `ReturnController` are thin wrappers over use cases that enforce business rules in aggregates like `Shift`, `SaleSession`, `Quotation`, and `SaleReturn`.\n- A **saga** built on top of outbox events coordinates with order, inventory, and payment modules when a quotation is confirmed, ensuring that orders, stock reservations, and payments remain consistent across services.\n- Tenant isolation, security, idempotency, and audit logging are consistently woven through these flows so the module can operate safely in a multi-tenant, bank-grade environment.

