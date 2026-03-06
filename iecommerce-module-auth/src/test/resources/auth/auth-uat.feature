Feature: Auth module UAT scenarios

  # UAT-AUTH-01..04 - User authentication & session
  Scenario: UAT-AUTH-01 Login with valid credentials
    Given a registered user exists with username "alice" and password "Password123!"
    And the account is ACTIVE and enabled
    When the client POSTs to "/api/v1/auth/login" with username "alice" and password "Password123!"
    Then the response status should be 200
    And the response body should contain a non-empty "access_token"
    And the response body should contain a non-empty "refresh_token"
    And calling a protected endpoint with that "access_token" should return 200

  Scenario: UAT-AUTH-02 Login with invalid password
    Given a registered user exists with username "alice" and password "Password123!"
    And the account is ACTIVE and enabled
    When the client POSTs to "/api/v1/auth/login" with username "alice" and password "WrongPassword"
    Then the response status should be 401
    And the response body should not contain any password or credential details
    And no access or refresh token should be returned

  Scenario: UAT-AUTH-03 Disabled user cannot log in
    Given a registered user "bob" exists and is DISABLED
    When the client POSTs to "/api/v1/auth/login" with bob's username and correct password
    Then the response status should be 401 or 403
    And the error message should indicate the account is disabled or locked

  Scenario: UAT-AUTH-04 Expired or invalid JWT is rejected
    Given an expired or tampered JWT access token
    When the client calls a protected endpoint with that token
    Then the response status should be 401
    And the "WWW-Authenticate" header should indicate an invalid or expired token

  # UAT-AUTH-10..12 - Account lock & rate limiting
  Scenario: UAT-AUTH-10 Lock account after too many failed attempts
    Given a user "alice" with correct password "Password123!"
    And the login lock threshold is 3 failed attempts
    When the client submits 3 consecutive login attempts with username "alice" and a wrong password
    Then the login lock store should record "alice" as locked
    And a subsequent login attempt with the correct password within the lock window
    Then the response status should be 429 or 403
    And the response should indicate the account is temporarily locked

  Scenario: UAT-AUTH-11 Rate limit login endpoint by IP
    Given the login rate limit is configured to N requests per minute per IP
    When the same IP sends N+1 POST requests to "/api/v1/auth/login" within one minute
    Then the final response status should be 429
    And the response body should contain "too_many_requests"
    And the "Retry-After" header should be present with a positive integer value

  Scenario: UAT-AUTH-12 Redis or Kafka unavailable does not break auth
    Given the system is configured with "auth.lock.store=redis" and Redis is temporarily unavailable
    When the client attempts to log in with valid credentials
    Then the login attempt should still succeed
    And the system should log a warning about the Redis store being unavailable
    And the rest of the authentication flow should continue without throwing 5xx errors

  # UAT-AUTH-20..22 - Roles, permissions, and access control
  Scenario: UAT-AUTH-20 Platform admin has full admin access
    Given a platform admin user with role "ROLE_PLATFORM_ADMIN"
    And the user has a valid JWT
    When the client calls an admin endpoint "/api/v1/admin/staff"
    Then the response status should be 200

  Scenario: UAT-AUTH-21 Tenant admin is scoped to their own tenant
    Given tenant A with a tenant admin user "adminA"
    And tenant B with a tenant admin user "adminB"
    And "adminA" has a JWT with tenantId "tenantA"
    When the client uses adminA's token to access tenant B resource "/api/v1/tenants/B"
    Then the response status should be 403 or 404
    And the user should not see tenant B data

  Scenario: UAT-AUTH-22 Permission changes take effect without restart
    Given a user "charlie" currently has permission "USER_READ" only
    And the user has a valid JWT
    When the client calls an endpoint requiring "USER_CREATE"
    Then the response status should be 403
    When an operator adds "USER_CREATE" permission to charlie's role
    And the client fetches a fresh token for "charlie"
    Then calling the same endpoint should now return 200

  # UAT-AUTH-30..33 - Tenant lifecycle
  Scenario: UAT-AUTH-30 Tenant signup
    Given no tenant exists with code "tenantX"
    When the client POSTs to "/api/v1/tenants/register" with code "tenantX" and name "Tenant X"
    Then the response status should be 201 or 202
    And a tenant domain object "tenantX" should be created with status TRIAL or PENDING_VERIFICATION
    And an initial TenantPreferences object should exist for "tenantX"

  Scenario: UAT-AUTH-31 Successful tenant provisioning
    Given a tenant "tenantY" in INITIAL provisioning status
    When the provisioning saga completes successfully
    Then the tenant status should become ACTIVE
    And the tenant provisioning status should be COMPLETED
    And users belonging to "tenantY" can authenticate and access tenant-specific APIs

  Scenario: UAT-AUTH-32 Tenant deactivation blocks access
    Given an ACTIVE tenant "tenantZ" with active users
    When the platform operator changes tenantZ status to DISABLED or TERMINATED
    And a user from tenantZ tries to call any protected API
    Then the response status should be 403
    And the error should indicate the tenant is disabled or terminated

  Scenario: UAT-AUTH-33 Tenant preferences update propagates
    Given a tenant "tenantX" with default preferences
    When the operator updates the tenant's preferences
    Then subsequent reads of tenantX preferences via the API should return the updated values

  # UAT-AUTH-40..42 - External integrations
  Scenario: UAT-AUTH-40 Bootstrap realm, clients, and routes
    Given Keycloak and Kong are running and empty
    When the application starts with "iecommerce.init.keycloak.enabled=true" and "iecommerce.init.kong.enabled=true"
    Then the logs should indicate the Keycloak realm "iecommerce" is created
    And Keycloak clients for web and admin are created if missing
    And the Kong service "iecommerce-backend" and route "iecommerce-api-route" are created and reachable

  Scenario: UAT-AUTH-41 Start app with Keycloak disabled
    Given Keycloak is not reachable
    And configuration sets "iecommerce.init.keycloak.enabled=false"
    When the application starts
    Then the application should start successfully
    And logs should state that Keycloak initialization is skipped due to configuration

  Scenario: UAT-AUTH-42 Start app with Kong disabled
    Given Kong is not reachable
    And configuration sets "iecommerce.init.kong.enabled=false"
    When the application starts
    Then the application should start successfully
    And logs should state that Kong initialization is skipped due to configuration

  # UAT-AUTH-50..51 - POS and staff flows
  Scenario: UAT-AUTH-50 POS terminal registration and session lifecycle
    Given a staff user with permission to manage POS terminals
    When the staff user registers a new POS terminal for tenant "tenantPOS"
    Then a new PosTerminal record should be created and associated with "tenantPOS"
    When a cashier logs in on that terminal and starts a POS session
    Then a PosSession should be created and marked ACTIVE
    And after logout or timeout the PosSession should be marked CLOSED or EXPIRED

  Scenario: UAT-AUTH-51 Staff permissions in POS vs web admin
    Given a staff user assigned to a specific tenant with limited permissions
    When the user accesses POS operations within that tenant
    Then only permitted operations should succeed and others should return 403
    When the same user attempts to access global admin endpoints
    Then the response status should be 403

  # UAT-AUTH-60..61 - Logging and observability
  Scenario: UAT-AUTH-60 No sensitive data in logs and business events at INFO
    Given logging.level.com.chamrong is set to INFO in the default profile
    When a user completes a full auth flow (login, change-password, logout)
    Then logs should contain entries for login success or failure and password change at level INFO
    And logs should not contain plaintext passwords, tokens, or other secrets

  Scenario: UAT-AUTH-61 Profiles adjust log volume and initializers
    Given the application is started with the default profile
    When observing logs during startup and normal traffic
    Then auth-module logs should appear mostly at INFO level with minimal DEBUG noise
    When the application is started with the "dev" profile
    Then additional DEBUG logs from the auth module should be visible
    And Keycloak and Kong initializers should honor "iecommerce.init.*.enabled" flags for dev

