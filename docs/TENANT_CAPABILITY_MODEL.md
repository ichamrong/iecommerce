# Tenant Capability Model

**Version:** 1.0  
**Purpose:** Govern allowed flows per tenant by vertical and plan without scattering if-statements.  
**References:** SAAS_ENTERPRISE_ARCHITECTURE_SPEC.md §1.2, §1.3; MODULE_COMPLETENESS_MATRIX.md (setting).

---

## 1. Overview

The SaaS supports multiple business types (E-commerce, POS, Accommodation, Hybrid). A tenant’s capabilities are determined by:

1. **Vertical mode** — which business type(s) are active  
2. **Enabled modules** — which modules (order, sale, booking, etc.) are allowed  
3. **Feature flags** — per-tenant and global (existing SettingService)  
4. **Plan quotas** — enforced by QuotaEnforcer (existing)

Capability checks must be enforced in one place (gate service or aspect) so that:

- Disabled modules return a stable error (e.g. 403 with `MODULE_DISABLED` or `VERTICAL_NOT_ALLOWED`)
- Plan/quota violations return a stable error (e.g. 402 or 403 with `QUOTA_EXCEEDED`)
- No business logic branches on “if (vertical == POS)” scattered across controllers

---

## 2. Capability Flags and Storage

### 2.1 Vertical mode

| Value | Meaning |
|-------|--------|
| `ECOMMERCE` | E-commerce only: order, catalog, inventory, customer, promotion, payment, invoice |
| `POS` | POS only: sale, shift, session, receipt, catalog, inventory, customer, promotion, payment |
| `ACCOMMODATION` | Accommodation only: booking, catalog (rooms), inventory (nights), customer (guest), payment, invoice |
| `HYBRID` | All of the above; inventory/conflict policy applies |

**Storage:** Tenant setting key `vertical_mode` (e.g. in `tenant_setting` or tenant profile in auth). Default per plan: e.g. Free = ECOMMERCE, Pro = ECOMMERCE or POS, Enterprise = HYBRID.

### 2.2 Enabled modules

Set of module identifiers that are allowed for the tenant. Derived from vertical mode and plan add-ons, or overridden per tenant.

| Module key | E-commerce | POS | Accommodation | Hybrid |
|------------|------------|-----|---------------|--------|
| order | ✅ | ✅ (POS order) | ✅ (booking-linked) | ✅ |
| sale | — | ✅ | — | ✅ |
| booking | — | — | ✅ | ✅ |
| catalog | ✅ | ✅ | ✅ | ✅ |
| inventory | ✅ | ✅ | ✅ | ✅ |
| customer | ✅ | ✅ | ✅ | ✅ |
| promotion | ✅ | ✅ | ✅ | ✅ |
| payment | ✅ | ✅ | ✅ | ✅ |
| invoice | ✅ | ✅ | ✅ | ✅ |
| report | ✅ | ✅ | ✅ | ✅ |
| setting | ✅ | ✅ | ✅ | ✅ |

**Storage:** Can be computed from `vertical_mode` + plan add-ons, or stored as `enabled_modules` (JSON array or comma-separated) in tenant setting.

### 2.3 Feature flags (existing)

- **SettingService.isFeatureEnabled(tenantId, featureKey)** — already exists.
- Use for fine-grained toggles (e.g. `accommodation.addons`, `pos.multi_terminal`, `invoice.digital_signature`).

### 2.4 Plan and quotas (existing)

- **QuotaEnforcer** — e.g. catalog product count, staff count, terminals.
- Plan limits: Free / Pro / Enterprise; add-on modules (e.g. Accommodation) can be gated by plan.

---

## 3. Enforcement

### 3.1 Where to enforce

- **Preferred:** Central gate (`TenantCapabilityService` in setting module implements `CapabilityGate` from `iecommerce-common.security`) called from:
  - An **aspect** on controller methods annotated with e.g. `@RequireModule("sale")` or `@RequireVertical(VerticalMode.POS)`, or
  - A **filter** that maps request path to module (e.g. `/api/sale/**` → module `sale`) and checks capability before controller.
- **Alternative:** Each controller calls `capabilityGate.requireModule(tenantId, "sale")` at entry; throws `CapabilityDeniedException` (from common.security) if disabled.

### 3.2 Stable error codes

| Code | HTTP | Meaning |
|------|------|---------|
| `MODULE_DISABLED` | 403 | Tenant’s vertical or plan does not allow this module |
| `VERTICAL_NOT_ALLOWED` | 403 | Operation not allowed for tenant’s vertical mode |
| `QUOTA_EXCEEDED` | 402 or 403 | Plan quota exceeded (e.g. products, staff, terminals) |
| `FEATURE_DISABLED` | 403 | Feature flag off for tenant |

### 3.3 Flow

1. Request arrives; tenant resolved from JWT (TenantContext).
2. Path → module mapping (e.g. `/api/sale/**` → `sale`).
3. Load tenant’s `vertical_mode` and/or `enabled_modules` (from setting or tenant profile).
4. If module not in enabled set for that vertical → 403 `MODULE_DISABLED`.
5. If quota check required (e.g. create product), call QuotaEnforcer; if exceeded → 402/403 `QUOTA_EXCEEDED`.
6. Optional: check feature flag for specific feature → 403 `FEATURE_DISABLED` if off.

---

## 4. Example configurations

### 4.1 E-commerce-only tenant

```json
{
  "vertical_mode": "ECOMMERCE",
  "enabled_modules": ["order", "catalog", "inventory", "customer", "promotion", "payment", "invoice", "report", "setting"]
}
```

- `/api/sale/*` → 403 MODULE_DISABLED  
- `/api/booking/*` → 403 MODULE_DISABLED  
- `/api/order/*`, `/api/catalog/*`, etc. → allowed (subject to quota).

### 4.2 POS-only tenant

```json
{
  "vertical_mode": "POS",
  "enabled_modules": ["sale", "catalog", "inventory", "customer", "promotion", "payment", "invoice", "report", "setting"]
}
```

- `/api/order/*` may be allowed for POS orders (same module, different flow) or restricted to “POS order” only; `/api/booking/*` → 403.

### 4.3 Accommodation-only tenant

```json
{
  "vertical_mode": "ACCOMMODATION",
  "enabled_modules": ["booking", "catalog", "inventory", "customer", "payment", "invoice", "report", "setting"]
}
```

- `/api/sale/*` → 403; `/api/order/*` allowed if booking-linked; `/api/booking/*` allowed.

### 4.4 Hybrid tenant

```json
{
  "vertical_mode": "HYBRID",
  "enabled_modules": ["order", "sale", "booking", "catalog", "inventory", "customer", "promotion", "payment", "invoice", "report", "setting"]
}
```

- All module endpoints allowed; inventory conflict policy applies (reservation vs POS vs eCommerce — see inventory docs).

---

## 5. Implementation notes

- **Default vertical_mode:** If not set, derive from plan (e.g. Free → ECOMMERCE) or default to ECOMMERCE.
- **SuperAdmin:** Can bypass module check for cross-tenant operations (separate auth).
- **Caching:** Cache per-tenant capability (vertical_mode, enabled_modules) with short TTL to avoid DB on every request.
- **Liquibase:** If storing in DB, add column or tenant_setting keys: `vertical_mode`, optionally `enabled_modules` (or compute from vertical + plan).

---

*End of TENANT_CAPABILITY_MODEL.md*
