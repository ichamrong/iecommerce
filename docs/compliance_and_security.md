# Compliance & Encryption Strategy

As an Enterprise SaaS Platform processing sensitive financial and personal data for third-party merchants, strict adherence to global compliance standards (e.g., GDPR, PCI-DSS, SOC2) is mandatory.

## 1. Data Encryption Strategy

### A. Encryption In-Transit (Network Level)
- **Rule**: All data moving between the client, API gateway, and internal microservices/modules MUST be encrypted via TLS 1.3 (HTTPS/WSS).
- **Internal Comms**: Database connections (PostgreSQL) and Cache connections (Redis) must enforce SSL/TLS encrypted links.

### B. Encryption At-Rest (Storage Level)
- **Database (TDE)**: The PostgreSQL database must utilize Transparent Data Encryption (TDE) at the filesystem/disk level. 
- **Object Storage**: Minio (S3) buckets storing Invoices, IDs, and private digital assets must have server-side encryption enabled (`SSE-KMS` or `SSE-S3`).

### C. Application-Level Encryption (Field-Level)
For highly sensitive data, relying solely on disk-level TDE is not enough. Specific fields must be encrypted directly within the application (Java/Hibernate) before being sent to the database.

**Fields requiring Application-Level Encryption (`@Convert(converter = CryptoConverter.class)`)**:
- `SettingValue` when `isSecret = true` (e.g., The Shop Admin's Telegram Bot Token, Stripe API Keys).
- `PaymentToken` or `BankAccountNumber` (if stored locally, though tokenization via Stripe is heavily preferred).
- `NationalIdNumber` or `PassportId` in the `Customer` and `Staff` modules.

## 2. Data hashing (One-Way)
Passwords are **never** stored in our PostgreSQL database. 
- Identity and Access Management (IAM) is fully delegated to **Keycloak**. 
- Keycloak handles the hashing of all user passwords using secure algorithms (e.g., `PBKDF2` or `Argon2`).

## 3. Data Immutability (Tamper-Proofing)

Certain domains in the platform represent legal or financial realities that **cannot be modified** once finalized. Attempting an `UPDATE` or `DELETE` on these records is strictly forbidden by the architecture.

### A. The Audit Trail (`audit-events` table)
- **Rule**: Purely `INSERT-only`. 
- **Reason**: To comply with ISO 27001 and SOC2, we must prove who did what. If an Admin creates a product, the `/audit` record of that creation can never be edited or soft-deleted.
- **Protection**: PostgreSQL database triggers should be configured to throw an exception on any `UPDATE` or `DELETE` command targeting this table.

### B. Financial Invoices (`invoices` table and PDFs)
- **Rule**: Once an invoice transitions from `PROFORMA` to `FINAL (TAX INVOICE)`, it becomes immutable.
- **Reason**: Legal accounting regulations. You cannot alter a receipt after the money has changed hands.
- **Correction Protocol**: If an invoice is wrong, it cannot be edited. A `CREDIT_NOTE` (refund) invoice must be generated, and a brand new `TAX_INVOICE` must be issued to establish a clean accounting trail.

### C. Financial Orders & Transactions
- **Rule**: Order Lines (the price of the item at the exact moment of sale) cannot be updated.
- **Reason**: If the Shop Admin changes the price of a T-Shirt from $10 to $15 today, a historical order from last week must still say $10. Modifying historical order totals corrupts financial reporting.

## 4. Right to be Forgotten (GDPR / Privacy)
Because we are a B2B2C platform, we process PII (Personally Identifiable Information).

### The Anonymization Protocol
When an End-Customer requests account deletion, we **cannot** simply `DELETE FROM customers WHERE id = X` because of strict Foreign Key constraints linking them to historical `Orders` and `Invoices` (which, as stated above, must remain immutable).

Instead, we employ **Hard Anonymization**:
1. Change `first_name` and `last_name` to `Anonymized User`.
2. Delete completely their `phone_number` and `Addresses`.
3. Overwrite `email` with a generated hash (e.g., `deleted-5f9a2b@system.local`).
4. Set User `deleted = true`.

This completely severs the PII from the user, satisfying GDPR, while keeping the financial records (The $50 order they placed) intact for the Shop Admin's tax reports.

## 5. eKYC (Electronic Know Your Customer) & AML
As a SaaS platform facilitating payments, we are legally required to prevent fraud, money laundering (AML), and illegal commerce. This requires automated Identity Verification (eKYC).

### A. Merchant eKYC (Shop Admins/Tenants)
Before a Shop Admin is allowed to move out of the "Free Trial" and accept real money via the `payment` module, their `TenantSubscription` status is locked to `PENDING_KYC`.
1. **Business Verification**: The Shop Admin must upload their Business Registration Certificate, Tax ID, and a Utility Bill (stored in the `asset` module using highly restrictive, encrypted S3 buckets).
2. **Identity Verification**: The primary business owner must upload a government ID (Passport/National ID) and perform a biometric liveness check (e.g., using a third-party service like SumSub, Onfido, or SmileID).
3. **Approval**: Only when the eKYC provider returns a "Verified" webhook does the SaaS platform set the Tenant to `ACTIVE_KYC`, unlocking their ability to configure production Stripe/Bakong payment gateways.

### B. End-Customer eKYC (Conditional)
For standard E-commerce (buying a T-Shirt), End-Customer eKYC is **not** required. However, the platform supports conditional eKYC for specific modules:
1. **Accommodation (`booking`)**: Hoteliers often require a copy of the Guest's Passport before check-in. The Guest must securely upload their ID via the `customer` module. This image is heavily encrypted and **auto-deleted** 30 days after check-out to minimize data liability.
2. **High-Value Retail (`order`)**: For merchants selling high-value goods (e.g., Rolex watches over $10,000), the `order` module can be configured to pause fulfillment until the End-Customer submits a frontend eKYC check to prevent credit card chargeback fraud.

## 6. Regional Compliance Considerations

Because this platform is designed as a borderless SaaS but deals with specific payment gateways, it explicitly architecturalizes logic for regional compliance constraints:

### A. European Union (GDPR)
- **Cookie Consent & tracking**: The headless frontend templates rely on explicit opt-in for marketing/tracking cookies (stored via the `customer` module preferences).
- **Data Portability**: End-Users must be able to download an export of all their PII and order history. The platform exposes a `/api/v1/customers/me/export` endpoint that aggregates data from `customer`, `order`, and `invoice` modules.
- **Right to Erasure**: Addressed comprehensively in Section 4.

### B. ASEAN & Local Banking Regulations (e.g., Cambodia / Central Bank)
- **Data Localization Laws**: Many ASEAN central banks mandate that citizen financial data must reside on domestic servers. The platform achieves this via strategic DevOps:
  - If operating in Cambodia, the production PostgreSQL database and Minio buckets are deployed to a local tier-1 data center (e.g., Ezecom or MekongNet) rather than a foreign AWS region.
- **Bakong / KHQR Integration**: To comply with the National Bank of Cambodia, the `payment` module natively supports dynamic KHQR generation. The QR codes encode the exact transaction value into the EMV-compliant string, preventing End-Customer manipulation of the payment amount.
- **Currency Compliance**: The `setting` module dictates a "Base Currency" but all transactions process parallel currency calculations (e.g., USD and KHR dual-pricing) as mandated by the Ministry of Commerce.
