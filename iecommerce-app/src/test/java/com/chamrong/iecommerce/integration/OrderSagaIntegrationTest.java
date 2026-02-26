package com.chamrong.iecommerce.integration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.chamrong.iecommerce.IecommerceApplication;
import com.chamrong.iecommerce.auth.application.dto.AuthResponse;
import com.chamrong.iecommerce.catalog.application.dto.CreateProductRequest;
import com.chamrong.iecommerce.catalog.application.dto.CreateProductRequest.CreateVariantRequest;
import com.chamrong.iecommerce.catalog.application.dto.CreateProductRequest.TranslationRequest;
import com.chamrong.iecommerce.catalog.application.dto.ProductResponse;
import com.chamrong.iecommerce.common.AbstractIntegrationTest;
import com.chamrong.iecommerce.inventory.application.dto.WarehouseRequest;
import com.chamrong.iecommerce.inventory.application.dto.WarehouseResponse;
import com.chamrong.iecommerce.order.application.dto.AddItemRequest;
import com.chamrong.iecommerce.order.application.dto.OrderResponse;
import com.chamrong.iecommerce.payment.application.dto.PaymentRequest;
import com.chamrong.iecommerce.payment.application.dto.PaymentResponse;

@SpringBootTest(
    classes = IecommerceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public class OrderSagaIntegrationTest extends AbstractIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  @DisplayName("Full Order Saga: Create -> Add Item -> Confirm -> Pay -> Pick -> Ship -> Complete")
  void testOrderSaga() throws InterruptedException {
    String tenantId = "TENANT_SAGA";

    // 1. Login as admin
    ResponseEntity<AuthResponse> loginResp =
        restTemplate.postForEntity(
            "/api/v1/auth/login",
            new com.chamrong.iecommerce.auth.application.command.LoginCommand(
                "admin", "admin", tenantId),
            AuthResponse.class);
    assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String token = loginResp.getBody().accessToken();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    headers.set("X-Tenant-Id", tenantId);

    // 2. Setup Catalog: Create a Product and Variant
    CreateProductRequest createProdReq = new CreateProductRequest(
        "test-phone-" + System.currentTimeMillis(),
        "PHYSICAL",
        new BigDecimal("999.00"), "USD",
        null, null,
        null, "STANDARD", "test",
        null, null,
        Map.of("en", new TranslationRequest("Test Phone", "Description", null, null, null)),
        List.of(new CreateVariantRequest("SKU-" + System.currentTimeMillis(), new BigDecimal("999.00"), "USD", null, null, null, 0, Map.of("en", "Default")))
    );

    ResponseEntity<ProductResponse> prodResp =
        restTemplate.exchange(
            "/api/v1/admin/products",
            HttpMethod.POST,
            new HttpEntity<>(createProdReq, headers),
            ProductResponse.class);
    assertThat(prodResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Long productId = prodResp.getBody().id();
    Long variantId = prodResp.getBody().variants().get(0).id();

    // 3. Publish Product (make it ACTIVE)
    restTemplate.exchange(
        "/api/v1/admin/products/" + productId + "/publish",
        HttpMethod.PATCH,
        new HttpEntity<>(headers),
        Void.class);

    // 4. Setup Inventory: Create Warehouse and Set Stock
    ResponseEntity<WarehouseResponse> whResp =
        restTemplate.exchange(
            "/api/v1/inventory/warehouses?tenantId=" + tenantId,
            HttpMethod.POST,
            new HttpEntity<>(new WarehouseRequest("Main WH", "CN"), headers),
            WarehouseResponse.class);
    Long whId = whResp.getBody().id();

    // Set stock to 100
    restTemplate.exchange(
        "/api/v1/inventory/stock/set?productId=" + variantId + "&warehouseId=" + whId + "&qty=100&tenantId=" + tenantId,
        HttpMethod.POST,
        new HttpEntity<>(headers),
        Void.class);

    // 5. Create Draft Order
    ResponseEntity<OrderResponse> createOrderResp =
        restTemplate.exchange(
            "/api/v1/orders",
            HttpMethod.POST,
            new HttpEntity<>(headers),
            OrderResponse.class);
    assertThat(createOrderResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Long orderId = createOrderResp.getBody().id();

    // 6. Add Item
    AddItemRequest itemReq = new AddItemRequest(variantId, 2, null, null, null, null);
    ResponseEntity<OrderResponse> addItemResp =
        restTemplate.exchange(
            "/api/v1/orders/" + orderId + "/items",
            HttpMethod.POST,
            new HttpEntity<>(itemReq, headers),
            OrderResponse.class);
    assertThat(addItemResp.getStatusCode()).isEqualTo(HttpStatus.OK);

    // 7. Confirm Order (triggers Inventory Reservation)
    restTemplate.exchange(
        "/api/v1/orders/" + orderId + "/confirm",
        HttpMethod.POST,
        new HttpEntity<>(headers),
        OrderResponse.class);

    // Wait for Outbox relay
    Thread.sleep(6000); 

    // 8. Pay for Order
    PaymentRequest payReq = new PaymentRequest(orderId, new BigDecimal("1998.00"), "USD", "STRIPE");
    ResponseEntity<PaymentResponse> payResp =
        restTemplate.exchange(
            "/api/v1/payments?tenantId=" + tenantId,
            HttpMethod.POST,
            new HttpEntity<>(payReq, headers),
            PaymentResponse.class);
    Long paymentId = payResp.getBody().id();

    restTemplate.exchange(
        "/api/v1/payments/" + paymentId + "/succeed?externalId=ext_saga_123",
        HttpMethod.POST,
        new HttpEntity<>(headers),
        PaymentResponse.class);

    Thread.sleep(6000); // Wait for saga (PaymentSucceeded -> Order Picking)

    // 9. Verify Order state
    ResponseEntity<OrderResponse> verifyResp =
        restTemplate.exchange(
            "/api/v1/orders/" + orderId,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            OrderResponse.class);
    assertThat(verifyResp.getBody().state()).isEqualTo("Picking");

    // 10. Finish lifecycle
    restTemplate.exchange("/api/v1/orders/" + orderId + "/pick", HttpMethod.POST, new HttpEntity<>(headers), OrderResponse.class);
    restTemplate.exchange("/api/v1/orders/" + orderId + "/pack", HttpMethod.POST, new HttpEntity<>(headers), OrderResponse.class);
    restTemplate.exchange("/api/v1/orders/" + orderId + "/ship?trackingNumber=SAGA123", HttpMethod.POST, new HttpEntity<>(headers), OrderResponse.class);
    restTemplate.exchange("/api/v1/orders/" + orderId + "/deliver", HttpMethod.POST, new HttpEntity<>(headers), OrderResponse.class);
    
    ResponseEntity<OrderResponse> completeResp =
        restTemplate.exchange(
            "/api/v1/orders/" + orderId + "/complete",
            HttpMethod.POST,
            new HttpEntity<>(headers),
            OrderResponse.class);
    assertThat(completeResp.getBody().state()).isEqualTo("Completed");
  }
}
