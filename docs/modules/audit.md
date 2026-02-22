# Module Specification: Audit

## 1. Purpose
The Audit module provides an immutable ledger of system activity. It tracks who performed what action, when, and on which entity, satisfying compliance requirements and aiding in debugging.

## 2. Core Domain Models
- **AuditEvent**: Complete, unmodifiable snapshot of a user action. 
  - **Identities**: `actorId`, `actorName`, `actorRole`.
  - **Targets**: `entityType`, `entityId`.
  - **Changes**: `changeDetails` as a JSON diff (before/after states).
  - **Context**: `ipAddress`, `userAgent`.

## 3. Key Business Logic
- **Immutability Enforcement**: `AuditEvent` exposes only getters. No mechanism exists to edit or delete historical data in the application layer.
- **Event Forwarding**: The Audit module is heavily subscriber-based. It listens for `ProductCreatedEvent`, `StaffSuspendedEvent`, `OrderCompletedEvent` and maps them automatically onto an `AuditEvent` payload without the source modules knowing.

## 4. Multi-Tenancy Strategy (SaaS)
- Data is entirely isolated by `tenant_id`. Admins of a tenant can see their own audit log, but no others.
- The `audit_events` table is **Range-Partitioned by Year**, ensuring fast retrieval of recent data despite rapid table growth since every transactional write generates an event log.

## 5. Public APIs (Internal Modulith)
- `AuditService.recordEvent(event)`: Manually save an event log payload.
- `AuditService.queryEvents(filter)`: Returns a paginated list of system events for a tenant.
