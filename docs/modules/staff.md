# Module Specification: Staff

## 1. Purpose
The Staff module handles the management of internal user profiles (employees) assigned to a tenant (merchant). It manages RBAC (Role-Based Access Control) and tracks organizational assignments.

## 2. Core Domain Models
- **StaffProfile**: Employee details including name, title, hire date.
- **StaffRole**: The level of access and permission assigned (e.g., `STORE_MANAGER`, `SALES_AGENT`, `CASHIER`, `SUPPORT`).
- **StaffStatus**: Active, Suspended, or Terminated status for the staff member.
- **Department**: The team unit a staff member belongs to (for larger merchants/enterprises).
- **AttendanceRecord (Time & Attendance)**: Tracks daily employee lifecycles for payroll.
  - *Data points*: `check_in_time`, `check_out_time`, `gps_location` (to verify they are physically at the shop), and `device_id`.
  - *Shifts*: Connects back to their assigned weekly schedule.

## 3. Key Business Logic
- **Suspension/Termination**: Suspending a staff member immediately generates an event to lock login access.
- **Role Scoping**: Certain roles manage specific physical locations/branches (e.g., store manager of Location A cannot access Location B's inventory).
- **Geospatial Check-ins**: When an employee clocks in via the mobile API, the system verifies their GPS coordinates against the store's physical address configuration to prevent fraud.

## 4. Multi-Tenancy Strategy (SaaS)
- Tenant Staff are scoped entirely to their respective `tenant_id`. Staff accounts cannot access data across tenants.

## 5. Public APIs (Internal Modulith)
- `StaffService.getProfile(id)`: Retrieve staff member specifics.
- `StaffService.suspend(id)`: Locks the staff account.
