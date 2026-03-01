package com.chamrong.iecommerce.promotion.api;

import com.chamrong.iecommerce.common.dto.CursorPage;
import com.chamrong.iecommerce.promotion.application.dto.PricingResponse;
import com.chamrong.iecommerce.promotion.application.dto.PromotionRequest;
import com.chamrong.iecommerce.promotion.application.dto.PromotionResponse;
import com.chamrong.iecommerce.promotion.application.dto.RedemptionRequest;
import com.chamrong.iecommerce.promotion.application.port.ApplyPromotionUseCase;
import com.chamrong.iecommerce.promotion.application.service.PromotionUseCaseService;
import com.chamrong.iecommerce.promotion.domain.model.PromotionStatus;
import com.chamrong.iecommerce.promotion.domain.rule.PromotionContext;
import com.chamrong.iecommerce.promotion.domain.rule.engine.PromotionEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Promotion management and voucher code validation. */
@Tag(name = "Promotions", description = "Discount rules and voucher code management")
@RestController
@RequestMapping("/api/v1/promotions")
@PreAuthorize("hasAuthority('promotions:read') or hasAuthority('promotions:manage')")
public class PromotionController {

  private final PromotionUseCaseService promotionService;
  private final ApplyPromotionUseCase redemptionService;

  public PromotionController(
      PromotionUseCaseService promotionService, ApplyPromotionUseCase redemptionService) {
    this.promotionService = promotionService;
    this.redemptionService = redemptionService;
  }

  @Operation(summary = "List all promotions with cursor pagination")
  @GetMapping
  public CursorPage<PromotionResponse> listAll(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(required = false) PromotionStatus status,
      @RequestParam(required = false) Long lastId,
      @RequestParam(defaultValue = "20") int limit) {
    return promotionService.listPromotions(tenantId, status, lastId, limit);
  }

  @Operation(summary = "Get promotion by ID")
  @GetMapping("/{id}")
  public ResponseEntity<PromotionResponse> getById(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable Long id) {
    return promotionService
        .getPromotion(tenantId, id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Calculate dynamic pricing for a cart")
  @PostMapping("/calculate")
  public ResponseEntity<PricingResponse> calculatePricing(
      @RequestHeader("X-Tenant-ID") String tenantId, @RequestBody Map<String, Object> attributes) {
    PromotionContext context = PromotionContext.fromAttributes(tenantId, attributes);
    PromotionEngine.PricingResult result = promotionService.calculate(context);

    PricingResponse response =
        new PricingResponse(
            result.totalBeforeDiscount(),
            result.totalDiscount(),
            result.totalAfterDiscount(),
            result.appliedPromotions());
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Validate a voucher code")
  @PostMapping("/validate/{code}")
  public ResponseEntity<Boolean> validateCode(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String code,
      @RequestBody Map<String, Object> contextData) {
    PromotionContext context = PromotionContext.fromAttributes(tenantId, contextData);
    return ResponseEntity.ok(promotionService.validate(tenantId, code, context));
  }

  @Operation(summary = "Reserve a promotion (Phase 1 of redemption)")
  @PostMapping("/reserve")
  public ResponseEntity<?> reserve(
      @RequestHeader("X-Tenant-ID") String tenantId, @RequestBody @Valid RedemptionRequest req) {
    redemptionService.reserve(
        tenantId,
        req.code(),
        req.orderId(),
        req.customerId(),
        req.redemptionKey(),
        PromotionContext.fromAttributes(tenantId, req.context()));
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @Operation(summary = "Create a promotion")
  @PostMapping
  @PreAuthorize("hasAuthority('promotions:manage')")
  public ResponseEntity<PromotionResponse> create(
      @RequestHeader("X-Tenant-ID") String tenantId, @RequestBody @Valid PromotionRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.create(tenantId, req));
  }

  @Operation(summary = "Update a promotion")
  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('promotions:manage')")
  public PromotionResponse update(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable Long id,
      @RequestBody @Valid PromotionRequest req) {
    return promotionService.update(id, req);
  }

  @Operation(summary = "Archive a promotion")
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('promotions:manage')")
  public ResponseEntity<Void> delete(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable Long id) {
    promotionService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
