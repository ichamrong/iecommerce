package com.chamrong.iecommerce.subscription.api;

import com.chamrong.iecommerce.subscription.application.SubscriptionService;
import com.chamrong.iecommerce.subscription.application.dto.SubscriptionPlanResponse;
import com.chamrong.iecommerce.subscription.application.dto.TenantSubscriptionResponse;
import com.chamrong.iecommerce.subscription.application.dto.UpgradeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Subscriptions", description = "SaaS plans and tenant subscription management")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  @Operation(summary = "List all available subscription plans")
  @GetMapping("/plans")
  public List<SubscriptionPlanResponse> getPlans() {
    return subscriptionService.getActivePlans();
  }

  @Operation(summary = "Get current subscription for a tenant")
  @GetMapping("/current")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<TenantSubscriptionResponse> getCurrent(@RequestParam String tenantId) {
    return subscriptionService
        .getTenantSubscription(tenantId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Start a free trial for a tenant")
  @PostMapping("/trial")
  @PreAuthorize("hasAuthority('tenant:admin')")
  public TenantSubscriptionResponse startTrial(
      @RequestParam String tenantId, @RequestParam String planCode) {
    return subscriptionService.startTrial(tenantId, planCode);
  }

  @Operation(summary = "Upgrade or change subscription plan")
  @PostMapping("/upgrade")
  @PreAuthorize("hasAuthority('tenant:admin')")
  public TenantSubscriptionResponse upgrade(
      @RequestParam String tenantId, @RequestBody UpgradeRequest request) {
    return subscriptionService.upgrade(tenantId, request);
  }

  @Operation(summary = "Cancel subscription auto-renewal")
  @PostMapping("/cancel")
  @PreAuthorize("hasAuthority('tenant:admin')")
  public TenantSubscriptionResponse cancel(@RequestParam String tenantId) {
    return subscriptionService.cancel(tenantId);
  }
}
