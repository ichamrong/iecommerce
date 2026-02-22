# Module Specification: Auth (IAM)

## 1. Purpose
The Auth module handles **Identity and Access Management (IAM)**. It is responsible for user authentication, role-based access control (RBAC), and multi-tenant identity isolation.

## 2. Core Concepts
- **User**: A person or system that can log in.
- **Tenant**: A business entity that owns data. A user can belong to multiple tenants.
- **Role**: A set of permissions (e.g., ADMIN, STORE_MANAGER, CUSTOMER).
- **Group**: A collection of users for easier management. 
  - *Dynamic Groups*: Groups like "GOLD_MEMBERS" or "VIP" are managed automatically by the **Promotion Module** based on loyalty rules.

## 3. Social Login & IdP Federation
One of the primary benefits of using Keycloak is **Identity Provider Federation**. This allows users to log in using:
- **Gmail (Google)**
- **Apple ID**
- **GitHub / Facebook / etc.**

### Keycloak Terminology: Federation vs. SSO
It is important to distinguish between these two related but different concepts:
- **IdP Federation (The Link)**: This is the specific mechanism used for "Login with Google/Apple." It means Keycloak **trusts** an external Identity Provider to verify who you are.
- **SSO (The Experience)**: Once you are logged into Keycloak (via Federation *or* a local password), you are signed into the **entire platform**. If we have multiple apps (e.g., Customer Web, Admin Panel, Mobile App), you don't have to log in again for each one.

**In summary**: Federation is the *tool* that connects us to Gmail/Apple, and SSO is the *benefit* where you log in once and the whole system knows you.

### Token Management (Stateless Architecture)
We use **Token Management (JWT)** instead of traditional server-side session management.
- **Why?**:
  - **Horizontal Scaling**: Our Resource Server (Spring Boot) is stateless. We can run 10 instances of the app without needing to sync sessions.
  - **Decoupling**: Keycloak manages the login "Session," but our API only cares about the validated token.
  - **Cross-Domain/Platform**: Tokens work seamlessly for web, mobile apps, and third-party integrations.
  - **Rich Claims**: Tokens carry our specific `tenant_id`, allowing immediate row-level security without extra DB lookups.

## 4. Keycloak Mapping Strategy
To keep the system synchronized, we map Keycloak concepts to our Domain Model as follows:

| **Concept** | **Keycloak Implementation** | **Internal Mapping (JWT Claim)** |
| :--- | :--- | :--- |
| **User** | Standard User Account | `sub` (UUID) -> `User.auth_id` |
| **Tenant** | User Attribute or Group | `tenant_id` claim (via Protocol Mapper) |
| **Role** | **Realm Roles** (Recommended) | `roles` claim -> Spring Security Authorities |
| **Permission** | N/A (App Side) | Mapped from Roles in internal logic |

### Why Realm Roles over Client Roles?
For our modular monolith, we recommend **Realm Roles**:
- **Simplicity**: One unified role set for the whole platform (Catalog, Order, etc.).
- **Consistency**: A "Manager" is a manager across the entire ecosystem.
- **Microservice Ready**: Even if we split into microservices later, we can still use shared Realm roles or map them easily.
- **Lower JWT Complexity**: Realm roles are at the top level of the JWT, making parsing faster and simpler.

## 5. Roles vs. Permissions
We follow the **RBAC (Role-Based Access Control)** pattern where roles are coarse-grained and permissions are fine-grained:

- **Role (Who you are)**: e.g., `STORE_MANAGER`, `CUSTOMER_SUPPORT`. These are managed in **Keycloak**.
- **Permission (What you can do)**: e.g., `product:edit`, `order:refund`. These are managed **internally** in the Java code.

### Performance: Permission Caching
To avoid querying the database for permissions on every single request, we use the following strategy:
1. **JWT (Token)**: Carries only the **Roles** (identity/authority).
2. **First Request**: When a user makes their first request, the `iecommerce-module-auth` looks up the permissions for their roles.
3. **Caching**: These permissions are cached in-memory (e.g., via Caffeine or Redis) for the duration of the token's lifetime (e.g., 5-15 minutes).
4. **Subsequent Requests**: Authorities are loaded directly from the cache.

## 6. Architecture: Adjacency List + Materialized Path
For our hierarchies (Teams, Categories), we use a hybrid approach:
1. **Adjacency List (`parent_id`)**: Used for all **Write** operations. It's the "Source of Truth" because moving a branch is simple and atomical.
2. **Materialized Path (`path`)**: Used for all **Read** operations. It is automatically updated via a JPA `@PostPersist`/`@PostUpdate` hook or a Database Trigger.

**Why this combination?**
- **Updates**: Changing a user's manager is just updating one `parent_id` and the `path` sub-tree (very fast compared to Nested Sets).
- **Reads**: Finding all subordinates is a simple indexed string search: `WHERE path LIKE '/1/5/%'`.
- **PostgreSQL Support**: PostgreSQL handles this extremely well with its index types (`btree` or `gist`).

## 7. Domain Model
- `User`: Linked to `sub (UUID)` from Keycloak.
- `Tenant`: Our internal record representing the merchant. When a new Tenant is created, an event triggers the `Subscription` module to assign a default Free Trial plan.
- `UserRole`: Mapping between Users, Roles, and Tenants.

## 8. Security Flow
1. Client requests token from **Keycloak**.
2. Client sends token to **Spring Boot API** in the Authorization header.
3. `iecommerce-common` extracts `tenant_id` and `user_id` into `TenantContext`.

## 9. Public APIs (Internal Modulith)
- `AuthService.getCurrentUser()`: Returns the authenticated user from the context.
- `AuthService.isAuthorized(Permission)`: Checks RBAC.
