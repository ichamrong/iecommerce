# Sale Module Specification

## 1. Overview
The Sale Module is responsible for managing Point of Sale (POS) operations, B2B sales quotations, and transaction intelligence. It complements the `order` module by handling physical storefront needs and sales pipeline management.

## 2. Core Entities

### 2.1 Sales Shift (`Shift`)
Tracks the lifecycle of a cashier's working period.
- **Attributes**: `staffId`, `terminalId`, `startTime`, `endTime`, `openingBalance`, `closingBalance`, `expectedBalance`, `status`.
- **Purpose**: Essential for cash reconciliation in physical stores, cafes, and hotels.

### 2.2 Sale Session (`SaleSession`)
Handles active customer interactions, especially for delayed checkout scenarios.
- **Attributes**: `shiftId`, `customerId`, `startTime`, `endTime`, `status`, `reference`.
- **Reference Examples**: Table Number (F&B), Room Number (Hospitality), Customer Name (Service).

### 2.3 Quotation (`Quotation`)
Handles B2B and wholesale sales proposals.
- **Attributes**: `customerId`, `expiryDate`, `totalAmount`, `status` (DRAFT, SENT, ACCEPTED, REJECTED, CONVERTED).
- **Items**: Links to `QuotationItem` containing product IDs, quantities, and negotiated unit prices.

### 2.4 Sales Return (`SaleReturn`)
Handles partial or full refunds and the issuance of Credit Notes.
- **Attributes**: `orderId`, `reason`, `refundAmount`, `status`.
- **Archiving**: Per tax compliance, returns must be linked back to the original invoice.

## 3. Digital Integrity & Compliance

### 3.1 Digital Signatures
In accordance with Cambodia's **Law on Electronic Commerce** and **Sub-Decree No. 246**, the following integrity measures are implemented:
- **Cryptographic Hashing**: Every issued invoice generates a digital signature using HMAC-SHA256 (or RSA in production).
- **Tamper Evidence**: If invoice data (Total, Date, Items) is modified, the signature validation will fail.
- **Signed Timestamp**: The `signedAt` field records the exact moment the document was finalized.

### 3.2 GDT / CamInvoice Readiness
The module is designed to integrate with the **CamInvoice clearance model**:
- **QR Code Placeholder**: Space for the GDT-validated QR code to be stored upon clearance.
- **UBL XML Format**: Infrastructure to export sale data in UBL XML format as required by the General Department of Taxation.

## 4. Key Workflows

1. **POS Flow**:
   - Staff starts a `Shift`.
   - Create a `SaleSession` for a table/room.
   - Add items via the `order` module.
   - Complete checkout -> Close session -> Finalize Invoice with digital signature.

2. **B2B Flow**:
   - Create `Quotation` -> Send to Customer.
   - Customer accepts -> Convert `Quotation` to `Order`.
   - Issue Invoice -> Collect Payment.

3. **Refund Flow**:
   - Customer requests return -> Verify original order and signature.
   - Create `SaleReturn` -> Approve -> Issue Credit Note.
