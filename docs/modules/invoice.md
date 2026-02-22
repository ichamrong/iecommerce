# Module Specification: Invoice

## 1. Purpose
The Invoice module manages the creation, storage, and retrieval of financial documents (invoices, receipts, proforma invoices) generated from orders and payments.

## 2. Core Domain Models
- **Invoice**: A final document issued to the customer after payment.
  - **Type**: `PROFORMA`, `TAX_INVOICE`, `RECEIPT`.
  - **Reference**: Links to an Order `id` or Payment `id`.
  - **Asset Url**: Pointer to the Minio stored PDF file.
- **InvoiceSequence**: Safely generates contiguous invoice numbers (e.g., INV-2026-0001) required by accounting regulations.

## 3. Key Business Logic
- **Immutability**: Once an invoice is generated and its PDF uploaded, it cannot be modified. Any changes require issuing a Credit Note.
- **Auto-Generation**: Listens to the `OrderPaidEvent` to automatically trigger PDF generation and upload.
- **Document Rendering**: Takes the structured JSON payload of the order, processes it through a template engine, and produces a PDF.

## 4. Multi-Tenancy Strategy (SaaS)
- Numbering sequences `INV-###` are guaranteed to be unique and monotonic per `tenant_id`. 
- Every tenant manages their own customized PDF template, displaying their specific company logo, VAT information, and legal footers.

## 5. Public APIs (Internal Modulith)
- `InvoiceService.generateInvoice(orderId)`: Forces a regeneration or initial generation of an invoice.
- `InvoiceService.getInvoiceUrl(orderId)`: Returns the signed download URL for the invoice document.
