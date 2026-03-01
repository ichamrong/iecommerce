# ASVS L2 Checklist

**Reference:** AUDIT_REMEDIATION_PLAN.md item 11, SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md §4.2.

---

## 1. Rate limiting (auth and payment)

| Area | Implementation | Notes |
|------|----------------|-------|
| Auth | `IpRateLimitFilter` (Bucket4j + Caffeine) in iecommerce-module-auth | Applied before login; configurable via `RateLimitProperties`. |
| Payment | `RateLimitingFilter` (iecommerce-common) | Protects paths containing `/payments` and `/webhooks` (token bucket per IP). |

Sensitive paths covered: `/login`, `/signup`, `/password`, `/webhooks`, `/payments`.

---

## 2. Webhook deduplication (payment)

- **Port:** `WebhookDeduplicationPort` (domain/webhook).
- **Adapter:** `JpaWebhookDedupAdapter`; stores `providerEventId` (or payload hash); unique constraint `uk_webhook_event_dedup` (changelog v19).
- **Usage:** `ProcessWebhookHandler` checks `dedupPort.isAlreadyProcessed(providerEventId)` before processing; duplicate events are skipped and metered (`payment.webhook.deduplicated`).

See iecommerce-module-payment/docs/WEBHOOK_VERIFICATION.md.

---

## 3. Invoice signature verification endpoint

- **GET** `/api/v1/invoices/{id}/verify` — Verifies stored signature for invoice by id (tenant-scoped). Returns `SignatureVerificationResponse` (signatureValid, reason).
- **POST** `/api/v1/invoices/verify` — Verifies by signature block (invoiceNumber, contentHash, signatureValue, keyId) for PDF-derived metadata.

Implemented in `InvoiceApplicationService.verifySignature` / `verifyBySignatureBlock`; `InvoiceController` exposes both.

---

## 4. Idempotency (audit of where applied)

| Module | Endpoint / flow | Key / store |
|--------|------------------|-------------|
| Order | POST /orders/{id}/confirm, /ship, /cancel | `Idempotency-Key` header; cached response for duplicate key. |
| Payment | Create payment intent | `PaymentIntent.idempotencyKey`; `findByIdempotencyKey` returns same intent. |
| Payment | Webhook | Event id dedup via `WebhookDeduplicationPort` (no double processing). |
| Invoice | Create / email | Idempotency key or composite (e.g. invoiceId:messageType) per SECURITY_AND_RELIABILITY_HARDENING.md. |
| Sale | Quotation | Idempotency supported where applicable. |

Idempotency keys are not logged in audit log by default; duplicate key use returns cached response (e.g. 200 with same body) or 409 where documented.

---

*Last updated: 2025-03-01.*
