# Module Specification: Payment

## 1. Purpose
The Payment module manages the integration with third-party payment gateways and handles the lifecycle of financial transactions. It provides a secure abstraction layer for both online and offline payment methods.

## 2. Core Domain Models
- **Payment**: The main aggregate recording a transaction attempt.
  - **Method**: `KHQR_BAKONG`, `STRIPE`, `PAY_ON_ARRIVAL`, `BANK_TRANSFER`.
  - **Status**: `PENDING`, `AUTHORIZED`, `CAPTURED`, `FAILED`, `REFUNDED`.
  - **Amount / Currency**: The total value and currency code.
- **PaymentProvider**: Orchestrates communication with external APIs.
- **RefundRecord**: Tracks the history and status of money being returned to a customer.

## 3. Supported Strategies

### A. Bakong (National Bank of Cambodia - KHQR)
Because Cambodia is a core market, **KHQR** (the unified national QR code standard) is a first-class citizen within the `payment` module.

- **Standard**: All generated QR strings must strictly adhere to the **EMV® QR Code Specification for Payment Systems (EMVCo)**.
- **Dynamic Generation (`PaymentProvider.generateKHQR`)**: 
  1. The API receives an `Order` calculation and the Tenant's specific Bakong Account ID.
  2. The module dynamically encodes the precise `Total Amount`, `Currency` (KHR/USD), and a unique `Terminal ID` (Order Number) into the raw KHQR string.
  3. The raw string is returned to the Headless Storefront to be rendered as a scannable QR Image.
- **Verification & Settlement**: 
  - **Primary (Webhook)**: The module exposes a secure endpoint (`/api/v1/payments/bakong/webhook`) to listen for real-time notifications from the Bakong API confirming the transaction was successful.
  - **Fallback (Polling)**: Because webhooks can drop due to network issues, the storefront (or backend cron job) can actively poll the Bakong API (`PaymentProvider.checkBakongStatus`) to verify the transaction `Hash` if the customer claims they paid but the webhook never arrived.

### B. Pay on Arrival (Offline)
- **Flow**: Confirmation happens immediately without a digital payment.
- **Order State**: The `Order` is marked as `PAID_OFFLINE` or `PAYMENT_PENDING_ARRIVAL`.
- **Closure**: The transaction is marked as `CAPTURED` manually by the staff (Receptionist/POS Operator) when the customer pays in person.

### C. Stripe / Credit Card
- **Flow**: standard redirect or embedded form for international credit/debit card processing.

## 4. Multi-Tenancy Strategy
- **Isolation**: Each tenant can configure their own API keys and credentials for Bakong, Stripe, etc.
- **Payment Methods**: Tenants can enable/disable specific methods (e.g., a "Guest House" might only allow Bakong and Pay on Arrival).

## 5. Public APIs (Internal Modulith)
- `PaymentService.initiatePayment(orderId, method)`: Creates a payment record and returns necessary data (like a QR string or a Redirect URL).
- `PaymentService.handleWebhook(payload)`: Processes incoming notifications from providers.
- `PaymentService.refundPayment(paymentId)`: Initiates a refund for a previously captured transaction.
- `PaymentService.captureOfflinePayment(paymentId)`: Manually confirms an offline payment.
