package com.chamrong.iecommerce.invoice.api;

import com.chamrong.iecommerce.invoice.application.InvoiceService;
import com.chamrong.iecommerce.invoice.application.dto.CreateInvoiceRequest;
import com.chamrong.iecommerce.invoice.application.dto.InvoiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Invoice management — creation, issuance, and payment tracking.
 *
 * <p>Base path: {@code /api/v1/invoices}
 */
@Tag(name = "Invoices", description = "Invoice creation and lifecycle management")
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('invoices:read') or hasAuthority('invoices:manage')")
public class InvoiceController {

  private final InvoiceService invoiceService;

  @Operation(summary = "Create a draft invoice for an order")
  @PostMapping
  @PreAuthorize("hasAuthority('invoices:manage')")
  public ResponseEntity<InvoiceResponse> create(
      @RequestParam String tenantId, @RequestBody CreateInvoiceRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(tenantId, req));
  }

  @Operation(summary = "Get invoice by ID")
  @GetMapping("/{id}")
  public ResponseEntity<InvoiceResponse> getById(@PathVariable Long id) {
    return invoiceService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get invoices for an order")
  @GetMapping("/orders/{orderId}")
  public List<InvoiceResponse> getByOrder(@PathVariable Long orderId) {
    return invoiceService.findByOrderId(orderId);
  }

  @Operation(summary = "Issue invoice", description = "Moves invoice from DRAFT → ISSUED and sets the invoice date.")
  @PostMapping("/{id}/issue")
  @PreAuthorize("hasAuthority('invoices:manage')")
  public InvoiceResponse issue(@PathVariable Long id) {
    return invoiceService.issue(id);
  }

  @Operation(summary = "Mark invoice as paid")
  @PostMapping("/{id}/pay")
  @PreAuthorize("hasAuthority('invoices:manage')")
  public InvoiceResponse markPaid(@PathVariable Long id) {
    return invoiceService.markPaid(id);
  }

  @Operation(summary = "Void an invoice")
  @PostMapping("/{id}/void")
  @PreAuthorize("hasAuthority('invoices:manage')")
  public InvoiceResponse voidInvoice(@PathVariable Long id) {
    return invoiceService.voidInvoice(id);
  }
}
