package com.chamrong.iecommerce.subscription.application;

import com.chamrong.iecommerce.subscription.SubscriptionApi;
import com.chamrong.iecommerce.subscription.application.dto.CreatePlanRequest;
import com.chamrong.iecommerce.subscription.application.dto.SubscriptionPlanResponse;
import com.chamrong.iecommerce.subscription.application.dto.TenantSubscriptionResponse;
import com.chamrong.iecommerce.subscription.application.dto.UpdatePlanRequest;
import com.chamrong.iecommerce.subscription.application.dto.UpgradeRequest;
import com.chamrong.iecommerce.subscription.domain.SubscriptionPlan;
import com.chamrong.iecommerce.subscription.domain.SubscriptionPlanRepository;
import com.chamrong.iecommerce.subscription.domain.TenantSubscription;
import com.chamrong.iecommerce.subscription.domain.TenantSubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionApi {

  private final SubscriptionPlanRepository planRepository;
  private final TenantSubscriptionRepository tenantSubscriptionRepository;

  // ── Plan Management (Admin) ────────────────────────────────────────────────

  /**
   * Returns all plans, including inactive ones.
   *
   * <p>Intended for backoffice administration screens.
   */
  @Transactional(readOnly = true)
  public List<SubscriptionPlanResponse> getAllPlans() {
    return planRepository.findAll().stream().map(this::toPlanResponse).toList();
  }

  /**
   * Creates a new subscription plan.
   *
   * @param request payload containing plan attributes
   * @return created plan representation
   */
  @Transactional
  public SubscriptionPlanResponse createPlan(CreatePlanRequest request) {
    SubscriptionPlan plan = new SubscriptionPlan();
    plan.setCode(request.code());
    plan.setName(request.name());
    plan.setDescription(request.description());
    plan.setPrice(request.price());
    plan.setMaxProducts(request.maxProducts());
    plan.setMaxOrdersPerMonth(request.maxOrdersPerMonth());
    plan.setMaxStaffProfiles(request.maxStaffProfiles());
    plan.setCustomDomainAllowed(request.customDomainAllowed());
    plan.setActive(request.active());

    SubscriptionPlan saved = planRepository.save(plan);
    log.info("Created subscription plan code={}", saved.getCode());
    return toPlanResponse(saved);
  }

  /**
   * Updates an existing subscription plan by id.
   *
   * @param planId identifier of the plan to update
   * @param request new attributes for the plan
   * @return updated plan representation
   */
  @Transactional
  public SubscriptionPlanResponse updatePlan(Long planId, UpdatePlanRequest request) {
    SubscriptionPlan plan =
        planRepository
            .findById(planId)
            .orElseThrow(() -> new EntityNotFoundException("Plan not found: " + planId));

    plan.setName(request.name());
    plan.setDescription(request.description());
    plan.setPrice(request.price());
    plan.setMaxProducts(request.maxProducts());
    plan.setMaxOrdersPerMonth(request.maxOrdersPerMonth());
    plan.setMaxStaffProfiles(request.maxStaffProfiles());
    plan.setCustomDomainAllowed(request.customDomainAllowed());
    plan.setActive(request.active());

    SubscriptionPlan saved = planRepository.save(plan);
    log.info("Updated subscription plan id={} code={}", saved.getId(), saved.getCode());
    return toPlanResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<SubscriptionPlanResponse> getActivePlans() {
    return planRepository.findByActiveTrue().stream().map(this::toPlanResponse).toList();
  }

  // ── Tenant Subscriptions ───────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public Optional<TenantSubscriptionResponse> getTenantSubscription(String tenantId) {
    return tenantSubscriptionRepository.findByTenantId(tenantId).map(this::toTenantResponse);
  }

  /** Initializes a free trial for a new tenant. */
  @Transactional
  public TenantSubscriptionResponse startTrial(String tenantId, String planCode) {
    SubscriptionPlan plan =
        planRepository
            .findByCode(planCode)
            .orElseThrow(() -> new EntityNotFoundException("Plan not found: " + planCode));

    TenantSubscription sub = TenantSubscription.startTrial(tenantId, plan, 14);

    log.info("Started trial for tenant={} plan={}", tenantId, planCode);
    return toTenantResponse(tenantSubscriptionRepository.save(sub));
  }

  /** Upgrades or changes the tenant's current plan. */
  @Transactional
  public TenantSubscriptionResponse upgrade(String tenantId, UpgradeRequest request) {
    SubscriptionPlan newPlan =
        planRepository
            .findByCode(request.planCode())
            .orElseThrow(
                () -> new EntityNotFoundException("Plan not found: " + request.planCode()));

    TenantSubscription sub =
        tenantSubscriptionRepository
            .findByTenantId(tenantId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("No active subscription for tenant: " + tenantId));

    // In a real app, logic for prorated billing would go here
    sub.upgradeTo(newPlan, Instant.now().plus(30, ChronoUnit.DAYS));

    log.info("Upgraded tenant={} to plan={}", tenantId, request.planCode());
    return toTenantResponse(tenantSubscriptionRepository.save(sub));
  }

  @Transactional
  public TenantSubscriptionResponse cancel(String tenantId) {
    TenantSubscription sub =
        tenantSubscriptionRepository
            .findByTenantId(tenantId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("No active subscription for tenant: " + tenantId));

    sub.cancel();
    log.info("Cancelled subscription for tenant={}", tenantId);
    return toTenantResponse(tenantSubscriptionRepository.save(sub));
  }

  /**
   * Resumes auto-renewal for a tenant's subscription.
   *
   * <p>If the subscription is expired, this method fails fast and the caller should create a new
   * subscription instead.
   */
  @Transactional
  public TenantSubscriptionResponse resume(String tenantId) {
    TenantSubscription sub =
        tenantSubscriptionRepository
            .findByTenantId(tenantId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException("No active subscription for tenant: " + tenantId));

    sub.resume();
    log.info("Resumed subscription for tenant={}", tenantId);
    return toTenantResponse(tenantSubscriptionRepository.save(sub));
  }

  // ── Quota Enforcement (SubscriptionApi) ───────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public void checkQuota(String tenantId, String quotaKey, long currentCount) {
    TenantSubscription sub =
        tenantSubscriptionRepository
            .findByTenantId(tenantId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No active subscription found for tenant: " + tenantId));

    if (!sub.isActive()) {
      throw new IllegalStateException("Tenant subscription is not active.");
    }

    SubscriptionPlan plan = sub.getPlan();
    int limit =
        switch (quotaKey) {
          case "maxProducts" -> plan.getMaxProducts();
          case "maxStaffProfiles" -> plan.getMaxStaffProfiles();
          case "maxOrdersPerMonth" -> plan.getMaxOrdersPerMonth();
          default -> Integer.MAX_VALUE;
        };

    if (currentCount >= limit) {
      throw new IllegalStateException(
          String.format(
              "Quota limit reached for '%s'. Current: %d, Limit: %d",
              quotaKey, currentCount, limit));
    }
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private SubscriptionPlanResponse toPlanResponse(SubscriptionPlan p) {
    return new SubscriptionPlanResponse(
        p.getId(),
        p.getCode(),
        p.getName(),
        p.getDescription(),
        p.getPrice(),
        p.getMaxProducts(),
        p.getMaxOrdersPerMonth(),
        p.getMaxStaffProfiles(),
        p.isCustomDomainAllowed(),
        p.isActive());
  }

  private TenantSubscriptionResponse toTenantResponse(TenantSubscription s) {
    return new TenantSubscriptionResponse(
        s.getId(),
        s.getTenantId(),
        s.getPlan().getCode(),
        s.getPlan().getName(),
        s.getStatus().name(),
        s.getStartDate(),
        s.getEndDate(),
        s.getNextBillingDate(),
        s.isAutoRenew());
  }
}
