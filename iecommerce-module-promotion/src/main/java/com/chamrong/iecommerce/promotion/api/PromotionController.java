package com.chamrong.iecommerce.promotion.api;

import com.chamrong.iecommerce.promotion.application.PromotionService;
import com.chamrong.iecommerce.promotion.application.dto.PromotionRequest;
import com.chamrong.iecommerce.promotion.application.dto.PromotionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Promotion management and voucher code validation.
 *
 * <p>Base path: {@code /api/v1/promotions}
 */
@Tag(name = "Promotions", description = "Discount rules and voucher code management")
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('promotions:read') or hasAuthority('promotions:manage')")
public class PromotionController {

  private final PromotionService promotionService;

  @Operation(summary = "List all promotions for a tenant")
  @GetMapping
  public List<PromotionResponse> listAll(@RequestParam String tenantId) {
    return promotionService.listAll(tenantId);
  }

  @Operation(summary = "Get promotion by ID")
  @GetMapping("/{id}")
  public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
    return promotionService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Validate a voucher code",
      description = "Returns the promotion if the code is active and valid at the current time.")
  @GetMapping("/validate")
  public ResponseEntity<PromotionResponse> validateCode(
      @RequestParam String code, @RequestParam String tenantId) {
    return promotionService
        .findActiveByCode(tenantId, code)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Create a promotion")
  @PostMapping
  @PreAuthorize("hasAuthority('promotions:manage')")
  public ResponseEntity<PromotionResponse> create(
      @RequestParam String tenantId, @RequestBody PromotionRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.create(tenantId, req));
  }

  @Operation(summary = "Update a promotion")
  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('promotions:manage')")
  public PromotionResponse update(@PathVariable Long id, @RequestBody PromotionRequest req) {
    return promotionService.update(id, req);
  }

  @Operation(summary = "Delete a promotion")
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('promotions:manage')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    promotionService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
