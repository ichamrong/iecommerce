# Epic: Core Identity and Profile Management

**Epic Link:** EPIC-100  
**Status:** In Progress  
**Description:** As a platform administrator, I need a unified but modular system to manage authentication, staff profiles, and customer profiles so that different user types can securely access and operate within the multi-tenant e-commerce platform according to their roles and permissions.

---

## User Persona Definitions
Before reading the stories, it is critical to understand the four distinct types of users this system manages:

1. **Superadmin (Platform Owner):** The ultimate administrator of the entire SaaS platform. They can provision enterprise tenants, create Staff members, and bypass standard limitations. They have `ROLE_PLATFORM_ADMIN`.
2. **Staff (SaaS Employee):** Internal employees hired by the Superadmin (e.g., Support Agents, Accountants). They act on behalf of the platform but are **strictly isolated** to view data only for the specific `tenantId`s assigned to them by the Superadmin.
3. **Tenant Owner (Business Owner):** A third-party merchant who signs up to use the software to run their shop. They own a specific `tenantId` but cannot access platform-level administrative tools. 
4. **Customer (Shopper):** A regular online shopper. Customers are **not global**; their profile is exclusively bound to the specific `tenantId` (Storefront) they registered on.

---

## User Story 1: Centralized Authentication & Bootstrapping
**Issue Type:** Story  
**Story Points:** 13  
**As a** system user, business owner, or superadmin  
**I want** a secure, central authentication module with proper system bootstrapping  
**So that** the system initializes securely, I can log in, businesses can register their shops natively, and customers can access specific tenant storefronts safely.

### Tasks:
1. **Task: Keycloak System Initialization & Auto-Configuration**
   - **Details:**
     - Integrate **Keycloak** as the primary Authorization Server (AS) for the platform.
     - On system startup, run an initializer that connects to the Keycloak Admin REST API to automatically create the Realm, define Client Scopes, and configure the necessary **OIDC Clients** (e.g., `iecommerce-web-client`, `iecommerce-admin-client`).
     - Provision the `superadmin` user in Keycloak and synchronize them down to our local Auth Module database with the `ROLE_PLATFORM_ADMIN` role.
     - Auto-configure Keycloak identity providers (Google/Social) if client credentials are present in the environment variables.
   - **Acceptance Criteria:**
     - Keycloak Realms, Roles, and fully-configured Clients exist automatically before any external traffic hits the API. No manual Keycloak UI configuration is required.
     - A master superadmin account is always available as the ultimate fallback in both Keycloak and the local database.

2. **Task: First Login Password Reset Policy (via Keycloak)**
   - **Details:**
     - Enforce a "requires_action: UPDATE_PASSWORD" flag for the bootstrapped `superadmin` user directly inside Keycloak.
   - **Acceptance Criteria:**
     - Keycloak intercepts the superadmin's first login attempt and forces them through a password reset flow before issuing a valid token.

3. **Task: Implement Core Auth Endpoints & Keycloak Sync**
   - **Details:** 
     - Develop `AuthController` (`POST /register`, `POST /login`, `POST /login/social`).
     - **Login:** The local `/login` endpoint acts as a proxy, securely passing credentials to Keycloak to receive an Access Token, or the frontend redirects directly to Keycloak to receive an OAuth2 token.
     - **Registration & Context:** Customer registration MUST accept and validate a `tenantId`. The Auth module registers the user in Keycloak, assigns the user to a Keycloak Tenant Group, and synchronizes the created user downward to our local database.
   - **Acceptance Criteria:**
     - Passwords must be managed and hashed exclusively by Keycloak.
     - The JWT issued by Keycloak clearly embeds the User ID, assigned authorities, and the isolated `tenantId` in its custom claims.

4. **Task: Implement Shopify-style Tenant Provisioning, Trials, & Suspension**
   - **Details:**
     - `POST /api/v1/tenants/register`: Public self-service endpoint. A **Tenant Owner** creates their shop independently. 
     - **Constraint (Free Trial):** Self-created tenants are automatically placed on a "Free Trial" status. The duration (e.g., 1 day, 5 days, 14 days) is globally configurable by the Superadmin.
     - `POST /api/v1/admin/tenants`: Protected endpoint (`tenant:create`). A **Superadmin** can bypass public logic to manually provision large enterprise tenants.
     - `PUT /api/v1/admin/tenants/{id}/status`: A Protected endpoint allowing the **Superadmin** or automated billing systems to dynamically `DISABLE` a tenant if their trial expires or their subscription goes unpaid.
   - **Acceptance Criteria:**
     - Shop names (slugs) must be globally unique across the entire SaaS platform.
     - A globally configurable Free Trial period is actively enforced for all self-service registrations.
     - If a tenant is marked as `DISABLED`, all subsequent authentication attempts (even with valid credentials) or API queries targeting that `tenantId` are strictly rejected as `403 Forbidden` across all storefronts and admin panels.

5. **Task: Define Fine-Grained Permissions & SPIs**
   - **Details:**
     - Annotate all endpoints with method security referencing a rigid `Permissions` constant file (`user:create`, `staff:manage`, etc.).
     - Define `UserRegisteredEvent`, `StaffAccountCreatedEvent`, and `StaffProfileClient` Interface in the root public package to comply tightly with Spring Modulith boundaries.
   - **Acceptance Criteria:**
     - System remains strictly modular; other packages listen via events rather than circular DB queries.

---

## User Story 2: Kong API Gateway Edge Security & Defense-in-Depth
**Issue Type:** Story  
**Story Points:** 8  
**As a** platform security engineer  
**I want** to route all traffic through a unified Kong API Gateway  
**So that** we can handle rate-limiting, edge-level JWT validation, and CORS globally before traffic even hits the internal microservices/modules.

### Tasks:
1. **Task: Deploy and Auto-Configure Kong API Gateway with Redis**
   - **Details:**
     - Set up Kong as the single entry point (`ingress`) for all external storefront and admin portal traffic.
     - On system startup, the Spring Boot application calls the Kong Admin API to automatically register its own upstream targets, services, and routing rules (forwarding `/api/v1/auth/**`, `/api/v1/admin/**`, and `/api/v1/customers/**`).
     - **DDoS Protection:** Auto-configure Kong's Rate Limiting plugin backed by a **Redis cluster**. This ensures the rate limit counters are distributed and resilient across all Kong edge nodes, preventing the backend from crashing during high-volume spikes or brute-force login attacks.
   - **Acceptance Criteria:**
     - Internal Spring Boot application is NOT exposed directly to the public internet; all traffic must pass through Kong.
     - Kong Services, Routes, and Redis-backed Rate Limiting Plugins are dynamically created on Boot if they do not exist. No manual Kong UI configuration is required.
     - The Spring Boot backend remains stable and unharmed during simulated DDoS attack loads due to Kong intercepting and dropping the requests at the edge.

2. **Task: Edge-Level JWT Validation via Kong (OIDC/Keycloak Plugin)**
   - **Details:**
     - Configure the Kong Keycloak/OIDC plugin to validate the signature and the expiration of the JWTs at the edge.
     - Once Kong validates the token, it forwards the request downstream with the parsed token headers so Spring Security can extract the specific `tenantId` and `roles`.
   - **Acceptance Criteria:**
     - Requests with missing or expired Keycloak tokens are rejected by Kong with a `401 Unauthorized` before using any backend JVM CPU cycles.

3. **Task: Configure Internal Spring Security Web Filter (Defense-in-Depth & Default-Deny)**
   - **Details:**
     - Even though Kong scans the token, Spring Security is configured as a `STATELESS` OAuth2 Resource Server to perform granular route-level authorization.
     - **Deny-by-Default:** Configure the `SecurityFilterChain` so that **every single endpoint** in the application requires authentication (`anyRequest().authenticated()`) by default.
     - Explicitly whitelist *only* the strictly necessary public endpoints (e.g., `POST /api/v1/auth/login`, `POST /api/v1/tenants/register`).
     - Configure a `JwtAuthenticationConverter` to extract Keycloak custom attributes into Spring GrantedAuthorities.
     - Apply method-level granular protection via `requestMatchers()`:
       - `/api/v1/admin/staff/**` requires `staff:manage`.
       - `POST /api/v1/admin/tenants` requires `tenant:create`.
       - `/api/v1/customers` requires `user:create` or `user:read` depending on the method.
   - **Acceptance Criteria:**
     - The system operates on a zero-trust, deny-by-default architecture. New endpoints added by developers in the future are automatically protected.
     - Authenticated edge traffic lacking explicit authority still returns `403 Forbidden` at the Spring filter level before hitting the controller.

---

## User Story 3: Staff Profile & Tenant Assignment Management (SaaS Administration)
**Issue Type:** Story  
**Story Points:** 8  
**As a** platform superadmin  
**I want** to create and manage dedicated administrator/staff accounts (e.g., Accounting, Support, Plan Managers)  
**So that** I can delegate SaaS management tasks while strictly isolating their access to only the specific tenants they are assigned to.

### Tasks:
1. **Task: Implement Role-Based Staff Creation & Keycloak Sync**
   - **Details:**
     - Build `StaffController` in `iecommerce-module-staff`.
     - Implement `POST /api/v1/admin/staff` to allow a superadmin to create a new staff profile.
     - The payload must explicitly assign specific role identifiers (e.g., `ROLE_ACCOUNTING`) via the Keycloak Admin API, followed by syncing the staff profile to the local database.
   - **Acceptance Criteria:**
     - A staff user receives only the permissions bound to their specific role inside Keycloak.
     - Staff profiles contain extended administrative metadata mapped directly to a root `Auth User ID` originating from Keycloak.

2. **Task: Enforce Strict Tenant Isolation for Staff via Keycloak Groups**
   - **Details:**
     - Implement `PUT /api/v1/admin/staff/{id}/tenants` to allow superadmins to bind staff members to an explicit list of `tenantId`s.
     - Push the assigned `tenantId`s up into the staff member's Keycloak User Attributes or Keycloak Groups so they become embedded in the generated JWT token payload automatically.
   - **Acceptance Criteria:**
     - The assigned `tenantId` list is passed securely inside the Keycloak JWT claims upon staff login.
     - Platform-level queries executed by the staff member are automatically filtered by their assigned tenants.
     - Updating a staff tenant assignment dynamically affects their Keycloak token on their next refresh.

3. **Task: Fulfill Modulith SPI Contract**
   - **Details:**
     - Create `StaffProfileClientImpl` that implements the Auth module's `StaffProfileClient`.
     - Wire this component via Spring to allow the Auth module to seamlessly query a staff member's assigned tenants during the JWT generation phase.
   - **Acceptance Criteria:**
     - Avoids circular HTTP requests or circular DB dependencies while ensuring the token always contains accurate tenant boundaries.

---

## User Story 4: Event-Driven Customer Auto-Provisioning
**Issue Type:** Story  
**Story Points:** 8  
**As a** Customer (Shopper)  
**I want** my shopper profile to be established automatically as soon as I register on a Tenant Owner's storefront  
**So that** I don't face additional onboarding friction before checking out my shopping cart.

### Tasks:
1. **Task: Listen for Keycloak Registration Events**
   - **Details:**
     - Create `AuthEventListener` inside `iecommerce-module-customer`.
     - Use Spring's `@TransactionalEventListener` or `@EventListener` to trap `UserRegisteredEvent` broadcasts that fire after a successful Keycloak registration sync.
   - **Acceptance Criteria:**
     - Listener fires reliably after the Auth user transaction commits locally.

2. **Task: Asynchronous CQRS Profile Creation with Strict Tenant Binding**
   - **Details:**
     - The `UserRegisteredEvent` payload MUST include the `tenantId` representing the specific **Tenant Owner's** shop, along with the external Keycloak `sub` (User ID).
     - Map the event payload to a `CreateCustomerCommand`.
     - Dispatch the command to the `CreateCustomerHandler` to spawn the local DB record in the customer module schema.
   - **Acceptance Criteria:**
     - **Customers are NOT global.** Every **Customer (Shopper)** profile must be explicitly bound to a `tenantId`. A single human registering on two different storefronts results in two isolated Customer profiles.
     - Profile contains matching ID constraints (Keycloak User ID -> internal mapping).
     - First and last names are gracefully handled as `null` since initial Social Logins or Keycloak emails might only contain email addresses.

3. **Task: Restructure Customer Module for Encapsulation**
   - **Details:**
     - Move all application logic into strict packages (`api`, `application.command`, `application.query`, `application.dto`).
     - Move the endpoint `CustomerController` to the `api` package.
     - Ensure queries to retrieve customers always filter by the `tenantId` derived from the active JWT context.
   - **Acceptance Criteria:**
     - Modulith Tests pass successfully. No domain logic leaks.
     - Tenant data isolation is physically maintained at the DB query level.

---

## User Story 5: Tenant Preferences & Storefront Customization
**Issue Type:** Story  
**Story Points:** 5  
**As a** Tenant Owner (Business Owner)  
**I want** to customize my storefront's dynamic settings (e.g., logo, brand colors, typography)  
**So that** my generated storefront visually represents my unique brand identity to my shoppers.

### Tasks:
1. **Task: Extend Tenant Domain with Preference Metadata**
   - **Details:**
     - Update the `Tenant` domain entity in `iecommerce-module-auth` to include a 1-to-1 relationship with a `TenantPreferences` entity (or a JSONB column).
     - The preferences payload should support standard UI properties: `logoUrl`, `primaryColor` (hex), `secondaryColor` (hex), and `fontFamily`.
   - **Acceptance Criteria:**
     - Preferences are seamlessly loaded when the Tenant profile is queried.
     - New tenants are provisioned with sensible default values (e.g., `#000000` for primary color, system sans-serif font).

2. **Task: Implement Tenant Preferences API Endpoints**
   - **Details:**
     - Build `PUT /api/v1/tenants/me/preferences` allowing the authenticated Tenant Owner to update their specific storefront settings.
     - Build a public `GET /api/v1/storefront/{tenantId}/preferences` endpoint allowing the Next.js/React frontend to anonymously fetch the colors and logos necessary to render the storefront before the user even logs in.
   - **Acceptance Criteria:**
     - The `PUT` endpoint strictly authorizes the user via their JWT, ensuring they can only modify their own `tenantId`'s preferences.
     - The public `GET` endpoint handles high traffic gracefully (ideally cached via Kong or Redis) since every storefront visitor hits it.
