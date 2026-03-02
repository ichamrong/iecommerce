# PAYMENT MODULE SPEC

**Module:** `iecommerce-module-payment`  
**Goal:** Bank-grade payment processing, webhook safety, and financial ledger integrity.

---

## 1. Architecture & Boundaries

- **Layers:** `api` / `application` / `domain` / `infrastructure`.
- **Domain rules:**
  - No Spring or JPA annotations in `domain`.
  - Aggregates:
    - `PaymentIntent` (pure Java, UUID id, tenant-scoped).
    - Legacy `Payment` entity (JPA) used for backward-compatible flows.
  - Value objects: `Money`, `FinancialLedgerEntry`, `PaymentTransaction`.
- **Ports (outbound):**
  - `PaymentIntentRepositoryPort`
  - `PaymentProviderPort`
  - `FinancialLedgerPort`
  - `WebhookVerificationPort`
  - `WebhookDeduplicationPort`
  - `PaymentOutboxPort`

---

## 2. Payment Intent Lifecycle

- **States:** `CREATED`, `PENDING`, `REQUIRES_ACTION`, `PROCESSING`, `SUCCEEDED`, `FAILED`, `CANCELLED`, `EXPIRED`, `REFUNDED`.
- **State machine:** implemented by `PaymentStateMachine`.
  - `CREATED|PENDING -> REQUIRES_ACTION` on provider checkout creation.
  - `CREATED|PENDING|REQUIRES_ACTION|PROCESSING -> SUCCEEDED` on authorization/capture.
  - `* -> FAILED` except from `SUCCEEDED` or `REFUNDED` (throws `PaymentException`).
  - `* -> CANCELLED` from non-terminal non-succeeded states.
  - `* -> EXPIRED` from non-terminal non-succeeded states.
  - `SUCCEEDED -> REFUNDED` (idempotent, second call is no-op); any other source throws `PaymentException`.
- **Domain enforcement:**
  - `PaymentIntent.start(...)` delegates to `PaymentStateMachine.onCreatedToRequiresAction`.
  - `PaymentIntent.succeed(...)` delegates to `onAuthorizedOrCaptured`.
  - `PaymentIntent.fail(...)` delegates to `onFailure`.
  - Legacy `Payment` uses the same state machine for `markSucceeded`, `markFailed`, `markRefunded`.

---

## 3. Idempotency

- **Create intent (API: `POST /api/v1/payments/intents`):**
  - Requires `idempotencyKey`.
  - DB unique constraint on `payment_intent.idempotency_key` (global idempotency).
  - `CreatePaymentIntentHandler` calls `PaymentIntentRepositoryPort.findByIdempotencyKey(...)` and returns the existing aggregate if present.
- **Webhooks:**
  - Idempotency at event level via `WebhookDeduplicationPort`.
  - DB unique constraint on `(provider, provider_event_id)` in `payment_webhook_event`.
  - Dedup is O(1) and safe for provider retries.
- **Refund / capture:**
  - Idempotency driven by provider semantics and state machine:
    - Re-processing a succeeded capture is a no-op at the domain level.
    - Re-processing a refund keeps the state at `REFUNDED` and does not throw.

---

## 4. Webhook Verification & Replay Protection

- **Ports & adapters:**
  - `WebhookVerificationPort` with provider-specific implementations under `infrastructure/*WebhookVerifier`.
  - `WebhookDeduplicationPort` backed by `WebhookEventEntity` and JPA.
- **Flow (`WebhookController` + `WebhookVerificationService` + `ProcessWebhookHandler`):**
  1. Receive raw request and headers.
  2. Verify signature and timestamp via `WebhookVerificationPort`.
  3. Check dedup via `WebhookDeduplicationPort.isAlreadyProcessed`.
  4. Map to a `VerificationResult` with `providerEventId`, `eventType`, `intentId`.
  5. `ProcessWebhookHandler`:
     - Loads `PaymentIntent` by `intentId`.
     - Maps provider event type to a `PaymentTransaction.TransactionType`.
     - Calls `intent.recordTransaction(...)`.
     - On success events (`payment_intent.succeeded`, `orders:completed`, etc.), posts a credit entry to the ledger via `FinancialLedgerPort`.
     - Persists updated intent and marks event processed via `WebhookDeduplicationPort.markAsProcessed`.
- **Replay protection:**
  - Subsequent identical events return early from `isProcessed` block without side effects but can still respond 200 to satisfy provider retry requirements.

---

## 5. Ledger Integrity (Double-Entry)

- **Domain object:** `FinancialLedgerEntry` (pure Java, immutable).
  - Fields: `entryId` (UUID), `tenantId`, `orderId`, `paymentIntentId`, `Money amount`, `LedgerType type`, `description`, `createdAt`.
  - Types: `CREDIT` (money in), `DEBIT` (money out).
- **Persistence:** `JpaFinancialLedgerAdapter` maps to `FinancialLedgerEntity` in table `financial_ledger`.
- **Invariants:**
  - Ledger is append-only: no in-place mutation of amounts or types.
  - Entries are written synchronously when webhook marks an intent as `SUCCEEDED`.
  - Currency and amount are taken from `PaymentIntent.amount` (no floats/doubles).

---

## 6. Cursor Pagination

- **Endpoint:** `GET /api/v1/payments/history`.
- **Implementation:**
  - Uses shared `CursorCodec`, `CursorPayload`, `FilterHasher`, `CursorPageResponse` from `iecommerce-common`.
  - Stable sort: `(created_at DESC, id DESC)` via `payment_intent` keyset index.
  - Filter binding:
    - Endpoint key: `payment:listIntents`.
    - Filters: `{ tenantId }` hashed into `filterHash`.
    - `CursorCodec.decodeAndValidateFilter` enforces cursor binds to the same tenant and endpoint.
  - Repository method: `PaymentIntentRepositoryPort.findNextPage(tenantId, lastCreatedAt, lastId, limit)` implements keyset pagination.

---

## 7. Liquibase & Schema

- **Changelogs:**
  - `v19-payment-hardening.xml` creates:
    - `payment_intent` with:
      - `id` UUID PK.
      - `tenant_id`, `order_id`, `amount`, `currency`, `provider`, `status`, `idempotency_key`, `external_id`, `checkout_url`, `client_secret`, `failure_code`, `failure_message`, `created_at`, `updated_at`, `version`.
      - Unique `idempotency_key` (`uk_payment_intent_idemp`).
      - Index `idx_payment_intent_tenant_created` on `(tenant_id, created_at, id)`.
    - `payment_ledger` for historical ledger entries.
    - `payment_webhook_event` with unique `(provider, provider_event_id)` for dedup.
- **@Version rules:**
  - `version` fields are managed by JPA; not initialized in code; DB column is non-null.

---

## 8. Logging, Metrics, and Security

- **Logging:**
  - Uses Slf4j with structured messages and stable log event keys under `LogEvents`.
  - No sensitive card or bank details written to logs.
- **Metrics (Micrometer):**
  - `payment.webhook.processed`
  - `payment.webhook.deduplicated`
  - `payment.outbox.relay.success`
  - `payment.outbox.relay.failure`
- **Security:**
  - Tenant isolation enforced by the caller (controller passes `X-Tenant-Id` and repository scopes).
  - Webhook verification is mandatory before any state change.
  - Idempotency and state machine protect against replay and double-capture.

---

## 9. UAT Scenarios

### 9.1 E-commerce Flow

1. Customer checks out an order.
2. API calls `POST /api/v1/payments/intents` with `idempotencyKey`.
3. Provider redirect / client SDK completes payment.
4. Provider sends webhook; verification + dedup succeed.
5. `PaymentIntent` transitions to `SUCCEEDED`; ledger entry is recorded.
6. Outbox publishes `PaymentSucceededEvent` for order and invoice modules.

### 9.2 POS / Immediate Payment

1. POS terminal posts `CreatePaymentIntent` (may use provider= CASH or terminal).
2. Payment is auto-succeeded by provider adapter.
3. Webhook (or synchronous confirmation) results in `SUCCEEDED` and ledger posting.

### 9.3 Refund

1. Operator triggers refund against a `SUCCEEDED` intent.
2. Provider adapter executes refund; webhook or direct callback marks intent as `REFUNDED`.
3. Ledger receives a `DEBIT` entry for the refund amount.
4. Replaying the refund callback keeps state at `REFUNDED` without exceptions.

### 9.4 Webhook Abuse / Replay

1. Attacker replays a valid previous webhook payload.
2. Dedup layer sees existing `provider_event_id` (or payload hash) and short-circuits.
3. No new ledger entries or state transitions occur.

---

*End of PAYMENT_MODULE_SPEC.md*

