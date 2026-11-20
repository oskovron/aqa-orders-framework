package com.example.autotests.api;

import com.example.autotests.client.OrdersClient;
import com.example.autotests.model.CreateOrderRequest;
import com.example.autotests.model.OrderResponse;
import com.example.autotests.model.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OrdersApiTest {

    private final OrdersClient orders = new OrdersClient();

    @Test
    void createdOrderCanBeFetchedById() {
        CreateOrderRequest request = new CreateOrderRequest("customer-" + UUID.randomUUID(), new BigDecimal("49.90"));
        OrderResponse created = orders.createOrder(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.customerId()).isEqualTo(request.customerId());
        assertThat(created.total()).isEqualByComparingTo(request.total());
        assertThat(created.status()).isEqualTo(OrderStatus.NEW);
        assertThat(created.createdAt()).isNotNull();

        OrderResponse fetched = orders.getOrder(created.id());

        assertThat(fetched).usingRecursiveComparison().isEqualTo(created);
    }
}
