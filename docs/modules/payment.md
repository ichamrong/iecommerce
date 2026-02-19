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

### A. Bakong (KHQR)
- **Flow**: The system generates a dynamic **KHQR string** for the specific order.
- **Verification**: The module listens for a webhook from Bakong or polls the Bakong API until the transaction is verified.
- **UI**: Displays the QR code to the customer during the checkout process.

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
