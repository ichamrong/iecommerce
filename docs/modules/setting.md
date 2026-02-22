# Module Specification: Setting

## 1. Purpose
The Setting module manages configurations that define how the application behaves for a specific **Tenant** or across the entire **System**.

## 2. Core Concepts
- **Global Setting**: Application-wide configurations (e.g., `supported_languages`, `maintenance_mode`).
- **Tenant Setting**: Configurations specific to a tenant (e.g., `store_name`, `base_currency`, `time_zone`).
- **Integration Settings**: External credentials and configurations:
  - **Email Settings**: SMTP Host, Port, Username, Password, Sender Name.
  - **Telegram Settings**: Bot Token, Chat ID (for system alerts).
  - **SMS Provider Settings**: 
    - **Active Provider**: (e.g., `TWILIO`, `VONAGE`).
    - **API Credentials**: Key, Secret, Sender ID.
  - **WhatsApp Settings**: API Token, Verified Number.
 details (e.g., Twilio).
  - **SMS**: Account SID, Auth Token, From Number.
  - **Push Notification**: Firebase (FCM) Server Key or OneSignal App ID.
- **Quotas & Limits**: Numeric limits defined per tenant or per subscription plan (e.g., `max_products`, `max_users_per_tenant`).

## 3. Quota Management Strategy
Instead of hardcoding limits in every module, we centralize them here:
1. **Definition**: Limits are stored as key-value pairs in the `tenant_settings` table.
2. **Retrieval**: Modules (Catalog, Auth, Asset) call `SettingService` to check if a tenant has reached their limit before performing a "Write" operation.
3. **Hierarchy**: If a tenant doesn't have a specific quota set, the system falls back to the **Global Default** or the **Plan Default**.

## 4. Relationship with Subscription Module
- **Subscription Module**: Knows *what plan* the tenant is on, their *billing status*, and their *feature flags* (via `TenantSubscription`).
- **Setting Module**: Knows the *numeric values* for any system limits tied to those feature plans (e.g., "Basic Plan" = 1,000 max products allowed). Setting Module serves as the fast lookup for simple threshold checks.

## 5. Domain Model
- `GlobalSetting`: `key`, `value`, `description`.
- `TenantSetting`: `tenant_id`, `key`, `value`, `category` (e.g., "INVENTORY", "MARKETING").

## 6. Public APIs (Internal Modulith)
- `SettingService.get(key)`: Returns the value for the current tenant.
- `SettingService.getQuota(quota_key)`: Returns the numeric limit.
- `SettingService.update(key, value)`: Updates a tenant-specific setting.
