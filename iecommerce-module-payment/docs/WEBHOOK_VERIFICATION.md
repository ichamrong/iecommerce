# Payment Webhook Verification

**Module:** iecommerce-module-payment  
**Reference:** ASVS L2, SAAS_ENTERPRISE_ARCHITECTURE_SPEC (webhook safety)

---

## Overview

Incoming payment provider webhooks (Stripe, PayPal, Bakong, ABA, etc.) are verified before processing. Verification ensures authenticity (signature) and supports idempotency (event id / payload hash deduplication).

## Port

- **`WebhookVerificationPort`** (domain/ports) — outbound port for verifying webhook payloads.
- Implementations live in infrastructure (e.g. `StripeWebhookVerifier`, `PayPalWebhookVerifier`, `BakongWebhookVerifier`, `AbaWebhookVerifier`).

## Verification Flow

1. **Controller / filter** receives raw body and headers, passes them to the appropriate verifier (by provider).
2. **Verifier** (implements `WebhookVerificationPort`):
   - Validates provider-specific signature (e.g. Stripe `Stripe-Signature`, PayPal headers).
   - Returns `VerificationResult`: `isValid`, `providerEventId`, `eventType`, `intentId`, `rawPayload`, `payloadHash`.
3. **Deduplication** uses `WebhookDeduplicationPort`: store/check `payloadHash` or `providerEventId` to avoid processing the same event twice.
4. **Processing** only runs when `isValid` is true and the event is not duplicate.

## Security Requirements

- **Signature verification:** Never process payloads with invalid or missing signature.
- **Deduplication:** Persist payload hash or provider event id; reject duplicates (return 200 OK without side effects to satisfy provider retries).
- **Tenant isolation:** Resolved PaymentIntent must belong to the tenant implied by the request (e.g. from payload or context).

## Provider Notes

- **Stripe:** Use `Stripe-Signature` and webhook signing secret; verify timestamp and signature.
- **PayPal:** Verify `PAYPAL-AUTH-ALGO`, `PAYPAL-CERT-URL`, `PAYPAL-TRANSMISSION-SIG`, `PAYPAL-TRANSMISSION-TIME`, `PAYPAL-TRANSMISSION-ID`.
- **Bakong / ABA:** Follow provider docs for signature or HMAC verification.

---

*See also: `WebhookVerificationPort`, `WebhookDeduplicationPort`, `ProcessWebhookHandler`.*
