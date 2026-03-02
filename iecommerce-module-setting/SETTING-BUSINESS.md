## Settings Module – Business Workflows & Architecture Map

### 1. Scope and Responsibilities

The settings module provides a **central configuration and capability backbone** for the entire ecommerce platform:

- **Global settings**: platform-wide configuration managed by administrators (e.g. supported languages, maintenance mode, default currency).
- **Tenant settings**: per-tenant overrides (e.g. store name, base currency, time zone, SMTP credentials).
- **Feature flags**: boolean toggles that can be enabled/disabled globally or per tenant.
- **Quotas / plan limits**: numeric limits such as `quota.max_products` or `quota.max_staff` enforced across modules.
- **Vertical mode & capabilities**: the `vertical_mode` setting and `VerticalMode` model define which business vertical(s) and modules a tenant may use (ECOMMERCE, POS, ACCOMMODATION, HYBRID).

This module is consumed by many others (order, catalog, sale, booking, etc.) to decide **what a tenant is allowed to do** and **how the system behaves** for that tenant.

---

### 2. APIs and Their Use Cases

Controllers live under:

- `iecommerce-api/iecommerce-module-setting/src/main/java/com/chamrong/iecommerce/setting/api`

#### 2.1 Tenant Settings – `TenantSettingController`

Base path: `/api/v1/tenants/me/settings`

- **List tenant settings**
  - **Endpoint**: `GET /api/v1/tenants/me/settings`
  - **Controller**: `TenantSettingController.getSettings`
  - **Use case**: `SettingService.getTenantSettings`
  - **Business flow**:
    - Reads all settings for the specified tenant id.
    - Applies category filter when provided.
    - Masks values for any setting marked `secret`, returning `SettingResponse.masked()`.

- **Upsert tenant setting**
  - **Endpoint**: `PUT /api/v1/tenants/me/settings/{key}`
  - **Controller**: `TenantSettingController.upsertTenantSetting`
  - **Use case**: `SettingService.upsertTenantSetting`
  - **Domain aggregate**: `TenantSetting`
  - **Business flow**:
    - If `(tenant, key)` exists, only non-null fields in `SettingRequest` are updated (partial update).
    - If not, a new `TenantSetting` is created.
    - Category and data type default to sensible values when not supplied.

- **Reset tenant setting to default**
  - **Endpoint**: `POST /api/v1/tenants/me/settings/{key}/reset`
  - **Controller**: `TenantSettingController.resetTenantSetting`
  - **Use case**: `SettingService.resetTenantSettingToDefault`
  - **Business flow**:
    - Deletes the tenant-specific override.
    - If a corresponding `GlobalSetting` exists, a new tenant record is seeded with that value and marked as “Reset to global default”.

- **Delete tenant setting**
  - **Endpoint**: `DELETE /api/v1/tenants/me/settings/{key}`
  - **Controller**: `TenantSettingController.deleteTenantSetting`
  - **Use case**: `SettingService.deleteTenantSetting`
  - **Business flow**:
    - Removes the `(tenant, key)` record completely.
    - Subsequent reads will fall back to global settings (if present) or behave as unset.

All tenant settings endpoints are protected by `settings:read` / `settings:manage` authorities.

#### 2.2 Global Settings – `GlobalSettingController`

Base path: `/api/v1/admin/settings`

- **List global settings**
  - **Endpoint**: `GET /api/v1/admin/settings`
  - **Controller**: `GlobalSettingController.getGlobalSettings`
  - **Use case**: `SettingService.getAllGlobalSettings` (or `getGlobalSettingsByCategory`)
  - **Business flow**:
    - Returns all global settings with masked secrets, optionally filtered by category.

- **Upsert global setting**
  - **Endpoint**: `PUT /api/v1/admin/settings/{key}`
  - **Controller**: `GlobalSettingController.upsertGlobalSetting`
  - **Use case**: `SettingService.upsertGlobalSetting`
  - **Domain aggregate**: `GlobalSetting`
  - **Business flow**:
    - Creates or updates the global setting identified by `key`.
    - Only non-null fields from `SettingRequest` overwrite existing values.

- **Delete global setting**
  - **Endpoint**: `DELETE /api/v1/admin/settings/{key}`
  - **Controller**: `GlobalSettingController.deleteGlobalSetting`
  - **Use case**: `SettingService.deleteGlobalSetting`

These endpoints are intended for platform operators and require `settings:manage` authority.

#### 2.3 Feature Flags – `FeatureFlagController`

Base path: `/api/v1/settings/features`

- **Check feature enabled**
  - **Endpoint**: `GET /api/v1/settings/features/{featureKey}?tenantId=...`
  - **Controller**: `FeatureFlagController.isFeatureEnabled`
  - **Use case**: `SettingService.isFeatureEnabled`
  - **Business flow**:
    - Evaluates `tenant setting → global setting → default "false"` for the given feature key.
    - Interprets string values case-insensitively as booleans (`"true"` ⇒ enabled).
    - Returns a small JSON object indicating the enabled state; callers can gate UI or workflows.

---

### 3. Domain Model and Capabilities

Domain classes live under:

- `iecommerce-api/iecommerce-module-setting/src/main/java/com/chamrong/iecommerce/setting/domain`

- **`GlobalSetting`**
  - Represents a **platform-wide configuration entry** (e.g., `supported_languages`, `maintenance_mode`).
  - Fields: `key`, `value`, `description`, `category`, `dataType`, `secret`, audit timestamps.

- **`TenantSetting`**
  - Represents a **per-tenant override** for a given setting key.
  - Uniquely constrained by `(tenant_id, setting_key)` so each tenant has at most one override per key.

- **`SettingCategory`**
  - Groups keys by business area: `GENERAL`, `EMAIL`, `SMS`, `WHATSAPP`, `TELEGRAM`, `PUSH_NOTIFICATION`, `QUOTA`, `SECURITY`, `PAYMENT`, `SHIPPING`, `APPEARANCE`, `FEATURE_FLAG`.

- **`SettingDataType`**
  - Declares the intended value type: `STRING`, `INTEGER`, `BOOLEAN`, `JSON`. Parsing and validation are currently performed in `SettingService` (e.g. `getQuota`).

- **`VerticalMode`**
  - Models the **business vertical** of a tenant: `ECOMMERCE`, `POS`, `ACCOMMODATION`, `HYBRID`.
  - Each mode has a set of allowed module codes (e.g. `"order"`, `"sale"`, `"booking"`).
  - Used together with the `vertical_mode` setting and `TenantCapabilityService` to gate module access.

---

### 4. Cross-Module Responsibilities: Capabilities and Quotas

#### 4.1 Tenant capabilities by vertical – `TenantCapabilityService`

Location:

- `iecommerce-api/iecommerce-module-setting/src/main/java/com/chamrong/iecommerce/setting/application/TenantCapabilityService.java`

Responsibilities:

- Implements `CapabilityGate` from `iecommerce-common` for **module-level access control**.
- Loads a tenant’s `vertical_mode` from the settings store (`KEY_VERTICAL_MODE = "vertical_mode"`).
- Resolves the enum `VerticalMode` and checks whether the requested module is allowed via `VerticalMode.allowsModule(module)`.
- Throws `CapabilityDeniedException.MODULE_DISABLED` when a module is not permitted for that tenant.

Other modules (e.g. sale, booking, order) can call `requireModule(tenantId, "sale")` to ensure vertical compatibility before executing business logic.

#### 4.2 Quota enforcement – `QuotaEnforcer`

Location:

- `iecommerce-api/iecommerce-module-setting/src/main/java/com/chamrong/iecommerce/setting/application/QuotaEnforcer.java`

Responsibilities:

- Provides a central way to enforce **per-tenant numeric limits**.
- Uses `SettingService.getQuota(tenantId, quotaKey)` to resolve:
  - Tenant override → global default → unlimited (`Integer.MAX_VALUE`).
- Exposes:
  - `enforce(tenantId, quotaKey, current)` – throws `QuotaExceededException` if current ≥ limit.
  - `isWithinQuota(tenantId, quotaKey, current)` – boolean check.

Other modules should call into this service whenever they create resources that consume quota (e.g. products, staff accounts, assets).

---

### 5. Summary for Business and Platform Owners

- The **settings module** centralizes configuration, feature flags, and quotas, ensuring consistent behaviour and gating across all other modules.
- Global and tenant settings provide a **fallback chain** (tenant → global → default) so plans and overrides can be modeled cleanly.
- **Vertical mode** and the capability gate act as a single source of truth for which modules a tenant is allowed to use, based on their business vertical and subscription.
- Quota enforcement is implemented as a shared service so per-tenant limits can be applied consistently wherever resources are created or used.

