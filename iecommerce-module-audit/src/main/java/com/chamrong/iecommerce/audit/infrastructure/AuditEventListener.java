package com.chamrong.iecommerce.audit.infrastructure;

import com.chamrong.iecommerce.audit.application.AuditService;
import com.chamrong.iecommerce.auth.TenantPreferencesUpdatedEvent;
import com.chamrong.iecommerce.auth.TenantRegisteredEvent;
import com.chamrong.iecommerce.auth.TenantStatusUpdatedEvent;
import com.chamrong.iecommerce.auth.UserDisabledEvent;
import com.chamrong.iecommerce.auth.UserLoggedInEvent;
import com.chamrong.iecommerce.auth.UserLoginFailedEvent;
import com.chamrong.iecommerce.auth.UserRegisteredEvent;
import com.chamrong.iecommerce.booking.BookingConfirmedEvent;
import com.chamrong.iecommerce.catalog.CategoryCreatedEvent;
import com.chamrong.iecommerce.catalog.CategoryDeletedEvent;
import com.chamrong.iecommerce.catalog.CategoryUpdatedEvent;
import com.chamrong.iecommerce.catalog.CollectionCreatedEvent;
import com.chamrong.iecommerce.catalog.CollectionUpdatedEvent;
import com.chamrong.iecommerce.catalog.FacetCreatedEvent;
import com.chamrong.iecommerce.catalog.FacetValueAddedEvent;
import com.chamrong.iecommerce.catalog.FacetValueRemovedEvent;
import com.chamrong.iecommerce.catalog.ProductAddedToCollectionEvent;
import com.chamrong.iecommerce.catalog.ProductArchivedEvent;
import com.chamrong.iecommerce.catalog.ProductCreatedEvent;
import com.chamrong.iecommerce.catalog.ProductDeletedEvent;
import com.chamrong.iecommerce.catalog.ProductPublishedEvent;
import com.chamrong.iecommerce.catalog.ProductRelationshipsUpdatedEvent;
import com.chamrong.iecommerce.catalog.ProductRemovedFromCollectionEvent;
import com.chamrong.iecommerce.catalog.ProductUpdatedEvent;
import com.chamrong.iecommerce.catalog.VariantAddedEvent;
import com.chamrong.iecommerce.catalog.VariantRemovedEvent;
import com.chamrong.iecommerce.catalog.VariantUpdatedEvent;
import com.chamrong.iecommerce.common.event.OrderCompletedEvent;
import com.chamrong.iecommerce.common.event.PaymentFailedEvent;
import com.chamrong.iecommerce.common.event.PaymentSucceededEvent;
import com.chamrong.iecommerce.common.event.StorageOperationEvent;
import com.chamrong.iecommerce.customer.AddressAddedEvent;
import com.chamrong.iecommerce.customer.AddressRemovedEvent;
import com.chamrong.iecommerce.customer.AddressUpdatedEvent;
import com.chamrong.iecommerce.customer.CustomerBlockedEvent;
import com.chamrong.iecommerce.customer.CustomerCreatedEvent;
import com.chamrong.iecommerce.customer.CustomerUnblockedEvent;
import com.chamrong.iecommerce.customer.CustomerUpdatedEvent;
import com.chamrong.iecommerce.staff.StaffCreatedEvent;
import com.chamrong.iecommerce.staff.StaffReactivatedEvent;
import com.chamrong.iecommerce.staff.StaffSuspendedEvent;
import com.chamrong.iecommerce.staff.StaffTerminatedEvent;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Listener for domain events across the system to populate audit logs. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

  private final AuditService auditService;

  private String getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
      return auth.getName();
    }
    return "SYSTEM";
  }

  // --- Catalog ---

  @EventListener
  public void onProductCreated(ProductCreatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "PRODUCT_CREATE",
        "PRODUCT",
        event.productId().toString(),
        "Tenant: " + event.tenantId());
  }

  @EventListener
  public void onProductUpdated(ProductUpdatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "PRODUCT_UPDATE",
        "PRODUCT",
        event.productId().toString(),
        "Tenant: " + event.tenantId());
  }

  @EventListener
  public void onProductPublished(ProductPublishedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "PRODUCT_PUBLISH",
        "PRODUCT",
        event.productId().toString(),
        "Tenant: " + event.tenantId());
  }

  @EventListener
  public void onProductArchived(ProductArchivedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "PRODUCT_ARCHIVE",
        "PRODUCT",
        event.productId().toString(),
        "Tenant: " + event.tenantId());
  }

  @EventListener
  public void onProductRelationshipsUpdated(ProductRelationshipsUpdatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "PRODUCT_RELATIONSHIPS_UPDATE",
        "PRODUCT",
        event.productId().toString(),
        "Tenant: " + event.tenantId());
  }

  @EventListener
  public void onProductDeleted(ProductDeletedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "PRODUCT_DELETE",
        "PRODUCT",
        event.productId().toString(),
        "Tenant: " + event.tenantId());
  }

  @EventListener
  public void onVariantAdded(VariantAddedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "PRODUCT_VARIANT_ADD",
        "PRODUCT",
        event.productId().toString(),
        "SKU: " + event.sku());
  }

  @EventListener
  public void onVariantUpdated(VariantUpdatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "PRODUCT_VARIANT_UPDATE",
        "PRODUCT",
        event.productId().toString(),
        "VariantID: " + event.variantId());
  }

  @EventListener
  public void onVariantRemoved(VariantRemovedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "PRODUCT_VARIANT_REMOVE",
        "PRODUCT",
        event.productId().toString(),
        "VariantID: " + event.variantId());
  }

  @EventListener
  public void onCategoryCreated(CategoryCreatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "CATEGORY_CREATE",
        "CATEGORY",
        event.categoryId().toString(),
        "Slug: " + event.slug());
  }

  @EventListener
  public void onCategoryUpdated(CategoryUpdatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "CATEGORY_UPDATE",
        "CATEGORY",
        event.categoryId().toString(),
        "Slug: " + event.slug());
  }

  @EventListener
  public void onCategoryDeleted(CategoryDeletedEvent event) {
    auditService.log(
        getCurrentUserId(), "CATEGORY_DELETE", "CATEGORY", event.categoryId().toString(), null);
  }

  @EventListener
  public void onCollectionCreated(CollectionCreatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "COLLECTION_CREATE",
        "COLLECTION",
        event.collectionId().toString(),
        "Slug: " + event.slug());
  }

  @EventListener
  public void onCollectionUpdated(CollectionUpdatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "COLLECTION_UPDATE",
        "COLLECTION",
        event.collectionId().toString(),
        null);
  }

  @EventListener
  public void onProductAddedToCollection(ProductAddedToCollectionEvent event) {
    auditService.log(
        getCurrentUserId(),
        "COLLECTION_ITEM_ADD",
        "COLLECTION",
        event.collectionId().toString(),
        "ProductID: " + event.productId());
  }

  @EventListener
  public void onProductRemovedFromCollection(ProductRemovedFromCollectionEvent event) {
    auditService.log(
        getCurrentUserId(),
        "COLLECTION_ITEM_REMOVE",
        "COLLECTION",
        event.collectionId().toString(),
        "ProductID: " + event.productId());
  }

  @EventListener
  public void onFacetCreated(FacetCreatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "FACET_CREATE",
        "FACET",
        event.facetId().toString(),
        "Name: " + event.name());
  }

  @EventListener
  public void onFacetValueAdded(FacetValueAddedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "FACET_VALUE_ADD",
        "FACET",
        event.facetId().toString(),
        "Code: " + event.code());
  }

  @EventListener
  public void onFacetValueRemoved(FacetValueRemovedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "FACET_VALUE_REMOVE",
        "FACET",
        event.facetId().toString(),
        "Code: " + event.code());
  }

  // --- Auth ---

  @EventListener
  public void onUserRegistered(UserRegisteredEvent event) {
    auditService.log(
        "SYSTEM",
        "USER_REGISTER",
        "USER",
        event.userId().toString(),
        "Username: " + event.username() + ", Tenant: " + event.tenantId());
  }

  @EventListener
  public void onUserLoggedIn(UserLoggedInEvent event) {
    auditService.log(
        event.username(), "USER_LOGIN", "USER", event.username(), "Tenant: " + event.tenantId());
  }

  @EventListener
  public void onUserLoginFailed(UserLoginFailedEvent event) {
    auditService.log(
        event.username(),
        "USER_LOGIN_FAILED",
        "USER",
        event.username(),
        "Reason: " + event.reason() + ", Tenant: " + event.tenantId());
  }

  @EventListener
  public void onUserDisabled(UserDisabledEvent event) {
    auditService.log(
        getCurrentUserId(),
        "USER_DISABLE",
        "USER",
        event.userId().toString(),
        "Tenant: " + event.tenantId());
  }

  @EventListener
  public void onTenantRegistered(TenantRegisteredEvent event) {
    auditService.log(
        "SYSTEM",
        "TENANT_REGISTER",
        "TENANT",
        event.tenantCode(),
        "Name: " + event.name() + ", Plan: " + event.plan());
  }

  @EventListener
  public void onTenantStatusUpdated(TenantStatusUpdatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "TENANT_STATUS_UPDATE",
        "TENANT",
        event.tenantCode(),
        "Status: " + event.status());
  }

  @EventListener
  public void onTenantPreferencesUpdated(TenantPreferencesUpdatedEvent event) {
    auditService.log(
        getCurrentUserId(), "TENANT_PREFERENCES_UPDATE", "TENANT", event.tenantCode(), null);
  }

  // --- Customer ---

  @EventListener
  public void onCustomerCreated(CustomerCreatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "CUSTOMER_CREATE",
        "CUSTOMER",
        event.customerId().toString(),
        "Email: " + event.email());
  }

  @EventListener
  public void onCustomerUpdated(CustomerUpdatedEvent event) {
    auditService.log(
        getCurrentUserId(), "CUSTOMER_UPDATE", "CUSTOMER", event.customerId().toString(), null);
  }

  @EventListener
  public void onCustomerBlocked(CustomerBlockedEvent event) {
    auditService.log(
        getCurrentUserId(), "CUSTOMER_BLOCK", "CUSTOMER", event.customerId().toString(), null);
  }

  @EventListener
  public void onCustomerUnblocked(CustomerUnblockedEvent event) {
    auditService.log(
        getCurrentUserId(), "CUSTOMER_UNBLOCK", "CUSTOMER", event.customerId().toString(), null);
  }

  @EventListener
  public void onAddressAdded(AddressAddedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "CUSTOMER_ADDRESS_ADD",
        "CUSTOMER",
        event.customerId().toString(),
        "Address: " + event.fullAddress());
  }

  @EventListener
  public void onAddressRemoved(AddressRemovedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "CUSTOMER_ADDRESS_REMOVE",
        "CUSTOMER",
        event.customerId().toString(),
        "AddressID: " + event.addressId());
  }

  @EventListener
  public void onAddressUpdated(AddressUpdatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "CUSTOMER_ADDRESS_UPDATE",
        "CUSTOMER",
        event.customerId().toString(),
        "AddressID: " + event.addressId());
  }

  // --- Staff ---

  @EventListener
  public void onStaffCreated(StaffCreatedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "STAFF_CREATE",
        "STAFF",
        event.staffId().toString(),
        "Email: " + event.email());
  }

  @EventListener
  public void onStaffSuspended(StaffSuspendedEvent event) {
    auditService.log(
        getCurrentUserId(), "STAFF_SUSPEND", "STAFF", event.staffId().toString(), null);
  }

  @EventListener
  public void onStaffTerminated(StaffTerminatedEvent event) {
    auditService.log(
        getCurrentUserId(), "STAFF_TERMINATE", "STAFF", event.staffId().toString(), null);
  }

  @EventListener
  public void onStaffReactivated(StaffReactivatedEvent event) {
    auditService.log(
        getCurrentUserId(), "STAFF_REACTIVATE", "STAFF", event.staffId().toString(), null);
  }

  // --- Order & Booking ---

  @EventListener
  public void onOrderCompleted(OrderCompletedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "ORDER_COMPLETE",
        "ORDER",
        event.orderId().toString(),
        "Tenant: " + event.tenantId());
  }

  @EventListener
  public void onBookingConfirmed(BookingConfirmedEvent event) {
    auditService.log(
        getCurrentUserId(),
        "BOOKING_CONFIRM",
        "BOOKING",
        event.bookingId().toString(),
        "Tenant: " + event.tenantId());
  }

  // --- Financial & Money Sequences ──────────────────────────────────────────

  @EventListener
  public void onPaymentSucceeded(PaymentSucceededEvent event) {
    auditService.logMonetaryChange(
        "SYSTEM", // Events don't carry the user ctx natively in async outbox relays
        "PAYMENT_SUCCEED",
        "PAYMENT",
        event.paymentId().toString(),
        BigDecimal.ZERO, // Before value conceptual (Pending)
        event.amount().getAmount(), // After value
        event.amount().getCurrency(),
        "Payment Succeeded. OrderID: " + event.orderId() + ", ExternalID: " + event.externalId());
  }

  @EventListener
  public void onPaymentFailed(PaymentFailedEvent event) {
    auditService.log(
        "SYSTEM",
        "PAYMENT_FAIL",
        "PAYMENT",
        event.paymentId().toString(),
        "Payment Failed. OrderID: " + event.orderId() + ", Reason: " + event.reason());
  }

  // --- Storage & Assets ---

  @EventListener
  public void onStorageOperation(StorageOperationEvent event) {
    String metadata =
        String.format(
            "Provider: %s, Duration: %dms, Status: %s, Message: %s",
            event.provider(), event.durationMs(), event.status(), event.errorMessage());

    auditService.log(
        getCurrentUserId(),
        "STORAGE_" + event.operation().toUpperCase(),
        "STORAGE",
        event.source(),
        metadata);
  }
}
