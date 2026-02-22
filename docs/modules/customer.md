# Module Specification: Customer

## 1. Purpose
The Customer module handles the storage and management of end-customer data, encompassing profiles, multiple addresses (shipping/billing), and loyalty/rewards.

## 2. Core Domain Models
- **CustomerProfile**: The primary aggregate containing identity and contact details.
- **Address**: A value object/entity storing physical location data, linked to a Customer Profile.
- **LoyaltyPoints**: Balances and transaction history for rewards programs.
- **LoyaltyTier**: Current status level (e.g., Bronze, Silver, Gold).

## 3. Key Business Logic
- **Profile Consolidation**: Reconciles guest checkouts with registered users via email/phone matching.
- **Loyalty Awards**: Listens to `OrderCompletedEvent` to calculate and automatically credit loyalty points to the customer account.
- **Status Management**: Customers can be active or blocked (suspension strategy).

## 4. Multi-Tenancy Strategy (SaaS)
- Customer databases are strictly isolated per `tenant_id` to ensure one merchant's customers exist exclusively in their own data silo.

## 5. Public APIs (Internal Modulith)
- `CustomerService.getProfile(id)`: Returns the full customer profile.
- `CustomerService.addAddress(id, address)`: Links a new shipping/billing address.
- `CustomerService.awardPoints(id, points)`: Increases the loyalty balance.
