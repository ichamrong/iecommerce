# User Acceptance Testing (UAT) & TDD Plan

## 1. TDD Strategy
We follow a **Test-Driven Development (TDD)** approach for every module:
1. **Red**: Write a failing UAT/Integration test based on the scenarios below.
2. **Green**: Implement the minimum code required to pass the test.
3. **Refactor**: Clean up the code while ensuring tests stay green.

---

## 2. Module-Specific UAT Scenarios

### [Auth] Security & Identity
- **UAT-AUTH-01**: User Registration & Social Login (Google/Apple).
- **UAT-AUTH-02**: Role-Based Access (Admin vs Customer).
- **UAT-AUTH-03**: Tenant Isolation (User from Tenant A cannot see Tenant B data).

### [Catalog] Product & Digital Assets
- **UAT-CAT-01**: Create Bookable Product with Grouped Image Gallery.
- **UAT-CAT-02**: Filter products by Dynamic Facets (e.g., "Sea View", "Pet Friendly").
- **UAT-CAT-03**: Validate House Rules and Capacity (Adults/Kids/Pets) are displayed.

### [Booking] Availability & Calendar
- **UAT-BOOK-01**: Successful date selection for a 3-night stay.
- **UAT-BOOK-02**: Multi-booking conflict (Prevent booking on already "Blocked" dates).
- **UAT-BOOK-03**: 15-minute Time Lock (Reservation expires if not paid).

### [Order] Lifecycle & Omnichannel
- **UAT-ORD-01**: Checkout flow for a Room + Optional Breakfast (Add-on).
- **UAT-ORD-02**: POS Order creation with "IMMEDIATE" stock deduction.
- **UAT-ORD-03**: Refund logic within the 7-day cancellation window.

### [Inventory] Stock & Audit
- **UAT-INV-01**: Partial stock adjustment (Write-off 5 items as "DAMAGED").
- **UAT-INV-02**: Multi-warehouse stock check for a specific variant.

### [Payment] Bakong & Offline
- **UAT-PAY-01**: Dynamic KHQR generation and webhook confirmation.
- **UAT-PAY-02**: "Pay on Arrival" flow – order remains "PAYMENT_PENDING" until staff capture.

### [Promotion] Rule Engine & Loyalty
- **UAT-PRO-01**: "Early Bird" discount (20% off) applied > 60 days before stay.
- **UAT-PRO-02**: "Last-Minute" deal (50% off) applied < 48h before arrival.
- **UAT-PRO-03**: Stacking Check (Ensure "Coupon" and "BOGO" follow priority rules).
- **UAT-PRO-04**: Loyalty Progression (Move to "Gold" group after spending $2,000).

### [Notification] Omnichannel Delivery
- **UAT-NOT-01**: Order confirmation sent via Email (HTML) and Telegram (Markdown).
- **UAT-NOT-02**: SMS provider failover (Attempt Twilio -> Fallback to local).

### [Setting] Multi-Tenant Config
- **UAT-SET-01**: Tenant-specific branding (Logo, SMTP, WhatsApp credentials).
- **UAT-SET-02**: Enforcing Quotas (Prevent adding 101st product if limit is 100).

### [Report] Analytics & Analytics
- **UAT-REP-01**: Daily Sales Report filtered by Region and Tenant.
- **UAT-REP-02**: Inventory Shrinkage report (Damage/Loss trends).

---

## 3. Execution Environment
- **Database**: PostgreSQL (Partitioned by Tenant/Date).
- **Identity**: Keycloak.
- **Testing Tools**: JUnit 5, Mockito, Testcontainers (for DB integration).
