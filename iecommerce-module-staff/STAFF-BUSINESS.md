## Staff Module – Business Workflows & Architecture Map

### 1. Scope and Responsibilities

The staff module manages **platform staff master data and lifecycle**:

- **Staff master data**: name, contact info, department, branch, and role.
- **Tenant assignments**: which tenants a staff member can manage (for multi-tenant SaaS).
- **Lifecycle**: `ACTIVE`, `SUSPENDED`, `TERMINATED` transitions.
- **Onboarding orchestration**: publishes staff account creation events to the auth module.
- **Audit trail**: records key staff actions for compliance and forensics.

It complements the auth/user module: auth owns login credentials and authentication, while the staff module owns business-facing staff profiles and permissions context (role + tenants).

---

### 2. APIs and Use Cases

API classes live under:

- `iecommerce-api/iecommerce-module-staff/src/main/java/com/chamrong/iecommerce/staff/api`

#### 2.1 Staff Administration – `StaffController`

Base path: `/api/v1/admin/staff`  
Security: `@PreAuthorize(Permissions.HAS_STAFF_MANAGE)`

- **List staff**
  - **Endpoint**: `GET /api/v1/admin/staff?cursor=&limit=`
  - **Controller**: `StaffController.listStaff`
  - **Use case**: `StaffQueryHandler.findAll`
  - **Business flow**:
    - Decodes an optional cursor token.
    - Calls `StaffRepositoryPort.findNextPage` for keyset pagination (`created_at DESC, id DESC`).
    - Returns `StaffCursorResponse<StaffResponse>` with `data`, `nextCursor`, and `hasNext`.

- **Get staff by ID**
  - **Endpoint**: `GET /api/v1/admin/staff/{id}`
  - **Controller**: `StaffController.getStaff`
  - **Use case**: `StaffQueryHandler.findById`
  - **Domain aggregate**: `StaffProfile`
  - **Business flow**:
    - Retrieves current tenant from `TenantContext.requireTenantId()`.
    - Loads a staff profile by id from `StaffRepositoryPort`.
    - If a tenantId is present and not in `assignedTenants`, returns “not found” semantics (to avoid cross-tenant leaks).
    - Maps to `StaffResponse` for the API.

- **Create staff (onboarding)**
  - **Endpoint**: `POST /api/v1/admin/staff`
  - **Controller**: `StaffController.createStaff`
  - **Command DTO**: `CreateStaffCommand`
  - **Use case**: `CreateStaffHandler.handle`
  - **Domain aggregate**: `StaffProfile`
  - **Business flow**:
    - Checks `StaffRepositoryPort.existsByUserId(username)` to prevent duplicate staff profiles.
    - Creates a `StaffProfile(username, fullName, role)` defaulting `null` role to `StaffRole.SUPPORT`.
    - Sets optional fields (phone, department).
    - Persists via `StaffRepositoryPort.save`.
    - Publishes:
      - `StaffAccountCreatedEvent` (auth module) with credentials and profile info.
      - `StaffCreatedEvent` (staff module) with staff id and email.
    - Records an audit log via `StaffAuditLogPort.save` with actor from `StaffSecurityContext.currentActorId()`.
    - Returns `StaffResponse`.

- **Update staff profile**
  - **Endpoint**: `PUT /api/v1/admin/staff/{id}`
  - **Controller**: `StaffController.updateProfile`
  - **Command DTO**: `UpdateStaffCommand` (extended with path id).
  - **Use case**: `UpdateStaffProfileHandler.handle`
  - **Domain aggregate**: `StaffProfile`
  - **Business flow**:
    - Loads profile by id using `StaffRepositoryPort`.
    - Calls `StaffProfile.updateProfile(fullName, phone, department, branch)` which checks that the staff is not `TERMINATED`.
    - Optionally updates role according to command.
    - Persists and returns `StaffResponse`, and appends an audit log entry.

- **Update tenant assignments**
  - **Endpoint**: `PUT /api/v1/admin/staff/{id}/tenants`
  - **Controller**: `StaffController.updateTenants`
  - **Command DTO**: `UpdateStaffTenantsCommand`
  - **Use case**: `UpdateStaffTenantsHandler.handle`
  - **Domain aggregate**: `StaffProfile`
  - **Business flow**:
    - Replaces `assignedTenants` set for the staff.
    - Saves profile and publishes `StaffTenantsUpdatedEvent` to auth so JWT claims / access policies can refresh.
    - Audits the change of tenant assignments.

- **Lifecycle operations (suspend, reactivate, terminate)**
  - **Suspend staff**
    - **Endpoint**: `PATCH /api/v1/admin/staff/{id}/suspend`
    - **Use case**: `SuspendStaffHandler.handle`
    - Calls `StaffProfile.suspend()`, which enforces not terminated.
    - Persists, publishes `StaffSuspendedEvent`, and writes audit.
  - **Reactivate staff**
    - **Endpoint**: `PATCH /api/v1/admin/staff/{id}/reactivate`
    - **Use case**: `ReactivateStaffHandler.handle`
    - Calls `StaffProfile.reactivate()`, which forbids reactivation if `TERMINATED`.
    - Persists, publishes `StaffReactivatedEvent`, and writes audit.
  - **Terminate staff**
    - **Endpoint**: `PATCH /api/v1/admin/staff/{id}/terminate`
    - **Use case**: `TerminateStaffHandler.handle`
    - Calls `StaffProfile.terminate()`, setting status to `TERMINATED` and stamping `terminationDate`.
    - Persists, publishes `StaffTerminatedEvent`, and writes audit.

#### 2.2 Error Handling – `StaffExceptionHandler`

Location:

- `iecommerce-module-staff/src/main/java/com/chamrong/iecommerce/staff/api/StaffExceptionHandler.java`

Responsibilities:

- Maps technical and domain errors to staff-specific HTTP responses:
  - `OptimisticLockingFailureException` → 409 `STAFF_CONFLICT`.
  - `IllegalStateException` → 409 `STAFF_INVALID_STATE`.
  - `IllegalArgumentException` → 409 `STAFF_ALREADY_EXISTS`.
  - `EntityNotFoundException` → 404 `STAFF_NOT_FOUND`.
  - Validation exceptions → 400 `STAFF_INVALID_INPUT`.
- Ensures stack traces and internal exception types are not leaked to API clients.

---

### 3. Domain Model & Lifecycle

Domain types live under:

- `iecommerce-module-staff/src/main/java/com/chamrong/iecommerce/staff/domain`

- **`StaffProfile`**
  - JPA aggregate for staff master data (`staff_profile` table), extending `BaseEntity`.
  - Fields:
    - `userId` (unique link to auth `User`), `fullName`, `phone`, `department`, `branch`.
    - `status` (`StaffStatus`), `role` (`StaffRole`).
    - `hireDate`, `terminationDate`.
    - `assignedTenants` (`@ElementCollection` table `staff_assigned_tenants`).
  - Lifecycle:
    - Constructor sets default role (SUPPORT) when `null` and status `ACTIVE` with `hireDate=now`.
    - `updateProfile(...)`:
      - Ensures staff is not terminated before applying changes.
    - `suspend()`:
      - Allowed only when not terminated; sets status to `SUSPENDED`.
    - `reactivate()`:
      - Forbids reactivation when `TERMINATED`; sets status to `ACTIVE` otherwise.
    - `terminate()`:
      - Sets status to `TERMINATED` and `terminationDate=now`.

- **`StaffStatus`**
  - Enum: `ACTIVE`, `SUSPENDED`, `TERMINATED`.

- **`StaffRole`**
  - Enum for business roles: e.g. `SUPPORT`, `STORE_MANAGER`, `SALES_AGENT`, `CASHIER` (see code for exact set).

- **`StaffAuditLog`**
  - Entity for an append-only audit trail: actor id, target staff id, action, timestamp.

- **Ports**
  - `StaffRepositoryPort`:
    - Abstracts staff persistence (find by id/userId, existence, save, paginated `findNextPage`).
  - `StaffAuditLogPort`:
    - Abstracts audit log storage.

---

### 4. Events and Cross-Module Interactions

The staff module communicates with other parts of the system via events and common utilities.

- **Auth / identity module**
  - Event: `StaffAccountCreatedEvent` (published on onboarding) – triggers creation of an auth user account and initial credentials.
  - Event: `StaffTenantsUpdatedEvent` – used by auth/JWT to refresh claims containing allowed tenants.

- **Internal staff events**
  - `StaffCreatedEvent`, `StaffSuspendedEvent`, `StaffReactivatedEvent`, `StaffTerminatedEvent`:
    - Emitted on respective lifecycle transitions so other modules (notifications, reporting, audit) can subscribe.

- **Common infrastructure**
  - `TenantContext`:
    - Used by `StaffController.getStaff` to scope reads to the current tenant.
  - `StaffSecurityContext`:
    - Extracts the acting user id (from JWT or defaults to `"system"`) for audit log entries.
  - `ApplicationEventPublisher`:
    - Dispatches both internal staff events and integration events for the auth module.

---

### 5. Summary for Business Stakeholders

- The **staff module** is the source of truth for staff profiles, their roles, their allowed tenants, and their lifecycle state.
- It provides **admin-only APIs** to list, view, create, update, and change the lifecycle of staff members, with all actions audited and many emitting events for other modules.
- Onboarding a staff member not only creates a profile but also **kicks off auth provisioning** via integration events.
- Lifecycle rules and tenant scoping are enforced in the domain and application layers, helping ensure safe, compliant management of staff across tenants.

