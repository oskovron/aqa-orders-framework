package com.example.autotests.api;

import com.example.autotests.client.OrdersClient;
import com.example.autotests.model.CreateOrderRequest;
import com.example.autotests.model.OrderResponse;
import com.example.autotests.model.OrderStatus;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.autotests.data.OrderRequestBuilder.anOrder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OrdersApiTest {

    private final OrdersClient orders = new OrdersClient();

    // ---------- 201 ----------

    @Test
    void createdOrderMatchesRequestAndCanBeFetchedById() {
        CreateOrderRequest request = anOrder().build();

        OrderResponse created = orders.createOrder(request);

        // Compare only the fields the client sent; BigDecimal by value (49.9 == 49.90)
        assertThat(created)
                .usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .comparingOnlyFields("customerId", "total")
                .isEqualTo(request);
        // Fields the server decides
        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(OrderStatus.NEW);
        assertThat(created.createdAt()).isNotNull();

        OrderResponse fetched = orders.getOrder(created.id());

        assertThat(fetched).usingRecursiveComparison().isEqualTo(created);
    }

    @Test
    void createReturnsLocationOfTheNewOrder() {
        Response response = orders.post(anOrder().build());

        assertThat(response.statusCode()).isEqualTo(201);
        OrderResponse created = response.as(OrderResponse.class);
        assertThat(response.header("Location")).endsWith("/orders/" + created.id());
    }

    // ---------- 400: validation ----------

    @ParameterizedTest(name = "customerId = [{0}] -> 400")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void invalidCustomerIdIsRejected(String customerId) {
        Response response = orders.post(anOrder().withCustomerId(customerId).build());

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @ParameterizedTest(name = "total = [{0}] -> 400")
    @NullSource
    @ValueSource(strings = {"0", "0.00", "-0.01"})
    void invalidTotalIsRejected(BigDecimal total) {
        Response response = orders.post(anOrder().withTotal(total).build());

        assertThat(response.statusCode()).isEqualTo(400);
    }

    // ---------- GET errors ----------

    @Test
    void unknownOrderIdReturns404() {
        assertThat(orders.get(UUID.randomUUID()).statusCode()).isEqualTo(404);
    }

    @Test
    void malformedOrderIdReturns400() {
        assertThat(orders.get("not-a-uuid").statusCode()).isEqualTo(400);
    }
}
